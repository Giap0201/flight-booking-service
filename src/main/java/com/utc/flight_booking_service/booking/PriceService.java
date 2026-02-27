package com.utc.flight_booking_service.booking;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.entity.Ticket;
import com.utc.flight_booking_service.booking.request.BookingFlightRequest;

import java.util.List;

public interface PriceService {
    List<Ticket> calculateTickets(Booking booking,
                                  List<BookingFlightRequest> bookingFlightRequests);

}
