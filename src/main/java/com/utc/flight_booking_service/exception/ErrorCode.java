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
    ROLE_NOT_FOUND(1100, "Role not found", HttpStatus.NOT_FOUND);

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
