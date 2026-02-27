package com.utc.flight_booking_service.booking.enums;

public enum BookingStatus {
    PENDING, // vua dat, dang giu ghe, chua thanh toan
    AWAITING_PAYMENT, // chuyen sang cong thanh toan
    PAID, // bao thanh toan thanh cong
    CONFIRMED, // xuat ve va da gui mail
    CANCELLED, // da huy
    REFUNDED, // hoan tien
}
