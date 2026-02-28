package com.utc.flight_booking_service.booking;

import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.BookingResponse;

import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse getBookingById (UUID id);

    void cancelExpiredBookings();
}
