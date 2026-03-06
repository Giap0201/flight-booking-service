package com.utc.flight_booking_service.booking.service;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import com.utc.flight_booking_service.booking.response.ClientETicketResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingById(UUID id);

    void cancelExpiredBookings();

    int deleteByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt);

    Booking getBookingEntityByPnr(String pnrCode);

    Booking getBookingEntityById(UUID id);

    void updateBookingStatus(UUID id, BookingStatus status);

    // Ham tao ve khi thanh toan thanh cong
    void issueTicketsForBooking(UUID bookingId);

    List<ClientETicketResponse> getTicketsByBookingId(UUID bookingId);
}
