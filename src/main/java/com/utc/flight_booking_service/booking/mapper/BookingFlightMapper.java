package com.utc.flight_booking_service.booking.mapper;

import com.utc.flight_booking_service.booking.entity.BookingFlight;
import com.utc.flight_booking_service.booking.request.BookingFlightRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingFlightMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "segmentNo", ignore = true)
    @Mapping(target = "originFlightNumber", ignore = true)
    @Mapping(target = "originDepartureTime", ignore = true)
    @Mapping(target = "originArrivalTime", ignore = true)
    BookingFlight toBookingFlight(BookingFlightRequest request);

}
