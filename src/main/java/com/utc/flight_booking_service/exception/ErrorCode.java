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


    //INVENTORY
    FLIGHT_NOT_FOUND(3000, "Không tìm thấy chuyến bay nào phù hợp", HttpStatus.NOT_FOUND),
    ORIGIN_REQUIRED(3001, "Điểm đi không được để trống", HttpStatus.BAD_REQUEST),
    DESTINATION_REQUIRED(3002, "Điểm đến không được để trống", HttpStatus.BAD_REQUEST),
    DATE_REQUIRED(3003, "Ngày bay không được để trống", HttpStatus.BAD_REQUEST),
    DATE_INVALID(3004, "Ngày bay phải từ hôm nay trở đi", HttpStatus.BAD_REQUEST),
    PASSENGERS_INVALID(3005, "Số lượng hành khách ít nhất là 1", HttpStatus.BAD_REQUEST)
    ;
    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
