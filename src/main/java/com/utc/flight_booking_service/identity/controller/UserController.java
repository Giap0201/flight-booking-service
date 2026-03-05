package com.utc.flight_booking_service.identity.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.identity.dto.request.*;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import com.utc.flight_booking_service.identity.service.IUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.addUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder().result(userService.getUsers())
                .build();
    }

    @GetMapping("/my-infor")
    public ApiResponse<UserResponse> getMyInfor() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") UUID userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@RequestBody UserUpdateRequest request, @PathVariable UUID userId) {

        return ApiResponse.<UserResponse>builder().result(userService.updateUser(request, userId))
                .build();
    }

    @PatchMapping("/{userId}/reset-password")
    ApiResponse<UserResponse> adminResetPassword(@RequestBody @Valid AdminPasswordResetRequest request, @PathVariable UUID userId) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.resetPasswordByAdmin(userId, request))
                .build();
    }

    @PatchMapping("/change-password")
    ApiResponse<UserResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.changePassword(request))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ApiResponse.<String>builder().result("Mật khẩu mới đã được gửi về email").build();
    }
}
