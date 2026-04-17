package com.utc.flight_booking_service.identity.service.impl;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.configuration.passwordGenerator;
import com.utc.flight_booking_service.identity.domain.entities.Role;
import com.utc.flight_booking_service.identity.domain.entities.User;
import com.utc.flight_booking_service.identity.domain.repository.RoleRepository;
import com.utc.flight_booking_service.identity.domain.repository.UserRepository;
import com.utc.flight_booking_service.identity.dto.request.*;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import com.utc.flight_booking_service.identity.mapper.UserMapper;
import com.utc.flight_booking_service.identity.service.IUserService;
import com.utc.flight_booking_service.notification.dto.NewPasswordEmailRequest;
import com.utc.flight_booking_service.notification.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    EmailService emailService;

    @Override
    public UserResponse addUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        HashSet<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public UserResponse getUser(UUID id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    public UserResponse updateUser(AdminUserUpdateRequest request, UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);

        var roles = roleRepository.findAllByNameIn(request.getRoles());
        if (roles.size() != request.getRoles().size()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));

    }

    @Override
    public UserResponse updateInfor(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findById(UUID.fromString(name)).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public UserResponse resetPasswordByAdmin(UUID userId, AdminPasswordResetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findById(UUID.fromString(name)).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse changePassword(ChangePasswordRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findById(UUID.fromString(name)).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCHED);
        boolean checkValid = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (!checkValid) throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String newPassword = passwordGenerator.generate(16);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        NewPasswordEmailRequest newPasswordEmailRequest = NewPasswordEmailRequest.builder()
                .newPassword(newPassword)
                .to(user.getEmail())
                .name(user.getFullName())
                .build();

        emailService.sendNewPasswordEmail(newPasswordEmailRequest);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();


        Pageable pageable = PageRequest.of(page, size, sort);


        Page<User> userPage = userRepository.findAll(pageable);


        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .data(userPage.getContent().stream()
                        .map(userMapper::toUserResponse)
                        .toList())
                .build();
    }

}
