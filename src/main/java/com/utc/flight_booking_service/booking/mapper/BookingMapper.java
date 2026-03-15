package com.utc.flight_booking_service.booking.mapper;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.client.BookingCreatedResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pnrCode", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currency", defaultValue = "VND")
    @Mapping(target = "expireAt", ignore = true)
    @Mapping(target = "passengers", ignore = true)
    @Mapping(target = "bookingFlights", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "totalFareAmount", ignore = true)
    @Mapping(target = "totalTaxAmount", ignore = true)
    @Mapping(target = "totalDiscountAmount", ignore = true)
    @Mapping(target = "bookingAncillaries", ignore = true)
    Booking toBooking(BookingRequest request);


    BookingCreatedResponse toBookingCreatedResponse(Booking booking);
}
