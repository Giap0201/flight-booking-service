package com.utc.flight_booking_service.identity.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.identity.dto.request.*;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    UserResponse addUser(UserCreationRequest request);

    List<UserResponse> getUsers();

    UserResponse getUser(UUID id);

    void deleteUser(UUID id);

    UserResponse updateUser(AdminUserUpdateRequest request, UUID id);

    UserResponse resetPasswordByAdmin(UUID userId, AdminPasswordResetRequest request);

    UserResponse getMyInfo();

    UserResponse changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    UserResponse updateInfor(UserUpdateRequest request);

    public PageResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir);
}