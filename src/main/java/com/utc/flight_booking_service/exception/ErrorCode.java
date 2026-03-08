package com.utc.flight_booking_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //SYSTEM - COMMON (1xxx)



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
    PASSWORD_INCORRECT(1017, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_NOT_MATCHED(1018, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATED(1019, "Mật khẩu không được trùng lặp với mật khẩu cũ", HttpStatus.BAD_REQUEST),


    // BOOKING
    FLIGHT_ID_REQUIRED(2001, "Mã máy bay không được bỏ trống", HttpStatus.BAD_REQUEST),
    FLIGHT_CLASS_ID_REQUIRED(2002, "Mã lịch bay không được bỏ trống", HttpStatus.BAD_REQUEST),
    FIRST_NAME_REQUIRED(2003, "Họ không được bỏ trống", HttpStatus.BAD_REQUEST),
    FIRST_NAME_TOO_LONG(2004, "Họ có độ dài không hợp lệ", HttpStatus.BAD_REQUEST),
    LAST_NAME_REQUIRED(2005, "Tên không được bỏ trống", HttpStatus.BAD_REQUEST),
    LAST_NAME_TOO_LONG(2006, "Tên có độ dài không hợp lệ", HttpStatus.BAD_REQUEST),
    DOB_REQUIRED(2007, "Ngày sinh không được bỏ trống", HttpStatus.BAD_REQUEST),
    DOB_MUST_BE_IN_PAST(2008, "Ngày sinh không hợp lệ", HttpStatus.BAD_REQUEST),
    GENDER_REQUIRED(2009, "Giới tính không được bỏ trống", HttpStatus.BAD_REQUEST),
    PASSENGER_TYPE_REQUIRED(2010, "Loại hành khách không được bỏ trống", HttpStatus.BAD_REQUEST),
    CANNOT_CREATE_PNR_CODE(2011, "Không thể tạo ra mã PNR_CODE", HttpStatus.INTERNAL_SERVER_ERROR),
    BOOKING_NOT_PAID_YET(2012, "Đơn đặt chưa được thanh toán hoặc đã bị huỷ", HttpStatus.BAD_REQUEST),
    BOOKING_CANCELLED(2013, "Đơn đặt đã bị huỷ", HttpStatus.BAD_REQUEST),
//    USER_REQUIRED (2014, "Không thể xác định người dùng", HttpStatus.INTERNAL_SERVER_ERROR),
    PNR_REQUIRED(2015, "Mã pnr không được bỏ trống", HttpStatus.BAD_REQUEST),
    CONTACT_EMAIL_REQUIRED(2016, "Email liên hệ không được trống", HttpStatus.BAD_REQUEST),
    CONTACT_NAME_REQUIRED(2017, "Tên liên hệ không được bỏ trống", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID_FORMAT(2018, "Email không đúng định dạng", HttpStatus.BAD_REQUEST),
    CONTACT_PHONE_REQUIRED(2019, "Số điện thoại liên hệ không được bỏ trống", HttpStatus.BAD_REQUEST),
    PHONE_INVALID_FORMAT(2020, "Số điện thoại liên hệ không đúng định dạng", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(2021, "Không tìm thấy Booking", HttpStatus.NOT_FOUND),
    FLIGHTS_REQUIRED(2022,"Danh sách chuyến đi không được rỗng", HttpStatus.BAD_REQUEST ),
    PASSENGERS_REQUIRED(2023, "Danh sách hành khách không được rỗng", HttpStatus.BAD_REQUEST ),
    CANNOT_CANCEL_BOOKING(2024, "Chỉ có thể hủy đơn đặt chỗ khi chưa thanh toán", HttpStatus.BAD_REQUEST),
    FORBIDDEN(2025, "Bạn không có quyền truy cập vào đơn đặt chỗ này", HttpStatus.FORBIDDEN),


    // TRANSACTION
    TRANSACTION_NOT_FOUND(2050, "Không tìm thấy Transaction", HttpStatus.NOT_FOUND),


    //INVENTORY
    FLIGHT_NOT_FOUND(3000, "Không tìm thấy chuyến bay nào phù hợp", HttpStatus.NOT_FOUND),
    ORIGIN_REQUIRED(3001, "Điểm đi không được để trống", HttpStatus.BAD_REQUEST),
    DESTINATION_REQUIRED(3002, "Điểm đến không được để trống", HttpStatus.BAD_REQUEST),
    DATE_REQUIRED(3003, "Ngày bay không được để trống", HttpStatus.BAD_REQUEST),
    DATE_INVALID(3004, "Ngày bay phải từ hôm nay trở đi", HttpStatus.BAD_REQUEST),
    PASSENGERS_INVALID(3005, "Số lượng hành khách ít nhất là 1", HttpStatus.BAD_REQUEST),
    UPDATE_SEAT_FAILED(3006, "Ghế đang được người khác đặt, vui lòng thử lại", HttpStatus.CONFLICT),
    NOT_ENOUGH_SEATS(3007, "Không đủ số lượng ghế trống", HttpStatus.BAD_REQUEST),
    MIN_SEAT_RESERVATION(3009, "Số lượng ghế đặt phải ít nhất là 1", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(3010, "Giá vé không được nhỏ hơn 0", HttpStatus.BAD_REQUEST)

    ;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
