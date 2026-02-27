package com.utc.flight_booking_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //SYSTEM - COMMON (1xxx)


    //BOOKING (2XXX)
    FLIGHT_ID_REQUIRED(2001, "Flight id required", HttpStatus.BAD_REQUEST),
    FLIGHT_CLASS_ID_REQUIRED(2002, "Flight class id required", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(1001, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1002, "Mã lỗi không hợp lệ", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_EXISTED(1003, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(1004, "Số điện thoại đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1014, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(1100, "Không tìm thấy vai trò", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(1101, "Vai trò đã tồn tại", HttpStatus.BAD_REQUEST),

    EMAIL_REQUIRED(1006, "Vui lòng nhập Email", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1007, "Định dạng Email không hợp lệ", HttpStatus.BAD_REQUEST),

    PASSWORD_REQUIRED(1008, "Vui lòng nhập mật khẩu", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1009, "Mật khẩu phải có độ dài từ {min} đến {max} ký tự", HttpStatus.BAD_REQUEST),

    FULLNAME_REQUIRED(1010, "Vui lòng nhập họ tên", HttpStatus.BAD_REQUEST),
    FULLNAME_INVALID(1011, "Họ tên phải có độ dài từ {min} đến {max} ký tự", HttpStatus.BAD_REQUEST),
    PHONE_REQUIRED(1012, "Vui lòng nhập số điện thoại", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1015, "Chưa xác thực danh tính", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1016, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    PHONE_INVALID(1013, "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0", HttpStatus.BAD_REQUEST),
    FIRST_NAME_REQUIRED(2003, "First name required", HttpStatus.BAD_REQUEST),
    FIRST_NAME_TOO_LONG(2004, "First name too long", HttpStatus.BAD_REQUEST),
    LAST_NAME_REQUIRED(2005, "Last name required", HttpStatus.BAD_REQUEST),
    LAST_NAME_TOO_LONG(2006, "Last name too long", HttpStatus.BAD_REQUEST),
    DOB_REQUIRED(2007, "Dob required", HttpStatus.BAD_REQUEST),
    DOB_MUST_BE_IN_PAST(2008, "Dob must be in past", HttpStatus.BAD_REQUEST),
    GENDER_REQUIRED(2009, "Gender required", HttpStatus.BAD_REQUEST),
    PASSENGER_TYPE_REQUIRED(2010, "Passenger type required", HttpStatus.BAD_REQUEST),

    CANNOT_CREATE_PNR_CODE(2011, "Can't create pnr code", HttpStatus.INTERNAL_SERVER_ERROR),
    BOOKING_NOT_FOUND(2021, "Booking not found", HttpStatus.NOT_FOUND),
    //INVENTORY
    FLIGHT_NOT_FOUND(3000, "Không tìm thấy chuyến bay nào phù hợp", HttpStatus.NOT_FOUND),
    ORIGIN_REQUIRED(3001, "Điểm đi không được để trống", HttpStatus.BAD_REQUEST),
    DESTINATION_REQUIRED(3002, "Điểm đến không được để trống", HttpStatus.BAD_REQUEST),
    DATE_REQUIRED(3003, "Ngày bay không được để trống", HttpStatus.BAD_REQUEST),
    DATE_INVALID(3004, "Ngày bay phải từ hôm nay trở đi", HttpStatus.BAD_REQUEST),
    PASSENGERS_INVALID(3005, "Số lượng hành khách ít nhất là 1", HttpStatus.BAD_REQUEST);


    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
