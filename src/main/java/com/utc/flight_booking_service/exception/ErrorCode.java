package com.utc.flight_booking_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //SYSTEM - COMMON (1xxx)
    UNCATEGORIZED_EXCEPTION(1001, "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1002, "Mã lỗi không hợp lệ", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_EXISTED(1003, "Email already exists", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(1004, "Phone already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "User not found", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1014, "User not existed", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(1100, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(1101, "Role already exists", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1006, "Email is required", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1007, "Invalid email format", HttpStatus.BAD_REQUEST),

    PASSWORD_REQUIRED(1008, "Password is required", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1009, "Password must be between 6 and 50 characters", HttpStatus.BAD_REQUEST),

    FULLNAME_REQUIRED(1010, "Full name is required", HttpStatus.BAD_REQUEST),
    FULLNAME_INVALID(1011, "Full name must be between 2 and 50 characters", HttpStatus.BAD_REQUEST),

    PHONE_REQUIRED(1012, "Phone number is required", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1015, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1016, "You do not have permission", HttpStatus.FORBIDDEN),
    PHONE_INVALID(1013, "Phone number must be 10 digits and start with 0", HttpStatus.BAD_REQUEST);

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
