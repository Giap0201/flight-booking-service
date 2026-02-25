package com.utc.flight_booking_service.identity.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.identity.dto.request.UserCreationRequest;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import com.utc.flight_booking_service.identity.service.UserService;
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
    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.addUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder().result(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") UUID userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

//    @PutMapping("/{userId}")
//    ApiResponse<UserResponse> updateUser(@RequestBody UserUpdateRequest request, @PathVariable UUID userId) {
//
//        //return
//        return ApiResponse.<UserResponse>builder().result(userService.updateUser(userId, request))
//                .build();
//    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }
}
