package com.utc.flight_booking_service.booking.enums;

public enum TicketStatus {
    RESERVED,   // Dang giu cho (BOOKING PENDING)
    ISSUED,     // Da xuat ve (BOOKING CONFIRMED)
    CHECKED_IN, // Da checkin
    CANCELLED,  // Huy ve (truoc khi thanh toan)
    REFUNDED    // Hoan ve (sau khi thanh toan)
}