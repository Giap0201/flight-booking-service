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
    PASSENGERS_INVALID(3005, "Số lượng hành khách ít nhất là 1", HttpStatus.BAD_REQUEST),
    UPDATE_SEAT_FAILED(3006, "Ghế đang được người khác đặt, vui lòng thử lại", HttpStatus.CONFLICT),
    NOT_ENOUGH_SEATS(3007, "Không đủ số lượng ghế trống", HttpStatus.BAD_REQUEST),
    FLIGHT_CLASS_ID_REQUIRED(3008, "ID hạng ghế không được để trống", HttpStatus.BAD_REQUEST),
    MIN_SEAT_RESERVATION(3009, "Số lượng ghế đặt phải ít nhất là 1", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(3010, "Giá vé không được nhỏ hơn 0", HttpStatus.BAD_REQUEST)
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
