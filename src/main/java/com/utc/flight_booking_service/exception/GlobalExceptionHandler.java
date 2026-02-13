package com.utc.flight_booking_service.exception;

import com.utc.flight_booking_service.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Bat toan bo loi chua duoc dinh nghia
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error(e.getMessage(), e);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION
                .getHttpStatusCode()).body(apiResponse);
    }

    // Bat cac loi da duoc dinh nghia trong ErrorCode
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handleAppException(AppException e) {
        log.error(e.getMessage(), e);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getErrorCode().getMessage())
                .build();
        return ResponseEntity.status(e.getErrorCode()
                .getHttpStatusCode()).body(apiResponse);
    }

    // Bat loi validate DTO khong hop le
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError != null) {
            String enumKey = fieldError.getDefaultMessage();
            try {
                errorCode = ErrorCode.valueOf(enumKey);
            } catch (IllegalArgumentException ex) {
                log.warn("Lỗi Validate: Không tìm thấy ErrorCode nào ứng với key {}", enumKey);
            }
        }
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode
                .getHttpStatusCode()).body(apiResponse);
    }
}
