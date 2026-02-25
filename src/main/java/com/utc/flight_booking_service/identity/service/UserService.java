package com.utc.flight_booking_service.identity.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.domain.entities.User;
import com.utc.flight_booking_service.identity.domain.repository.UserRepository;
import com.utc.flight_booking_service.identity.dto.request.UserCreationRequest;
import com.utc.flight_booking_service.identity.dto.request.UserUpdateRequest;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import com.utc.flight_booking_service.identity.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse addUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserRespone(userRepository.save(user));
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserRespone).toList();
    }

    public UserResponse getUser(UUID id) {
        return userMapper.toUserRespone(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    public void updateUser(UserUpdateRequest request) {

    }
}
