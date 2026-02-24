package com.utc.flight_booking_service.booking;

import com.utc.flight_booking_service.booking.entity.BookingFlight;
import com.utc.flight_booking_service.booking.request.BookingFlightRequest;
import com.utc.flight_booking_service.booking.response.BookingFlightResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingFlightMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "priceAtBooking", ignore = true)
    @Mapping(target = "booking", ignore = true)
    BookingFlight toBookingFlight(BookingFlightRequest request);

    BookingFlightResponse toBookingFlightResponse(BookingFlight bookingFlight);
}
