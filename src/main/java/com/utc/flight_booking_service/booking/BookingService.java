package com.utc.flight_booking_service.booking;

import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.BookingResponse;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
}
