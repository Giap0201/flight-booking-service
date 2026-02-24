package com.utc.flight_booking_service.booking.mapper;

import com.utc.flight_booking_service.booking.entity.Passenger;
import com.utc.flight_booking_service.booking.request.PassengerRequest;
import com.utc.flight_booking_service.booking.response.PassengerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "type", ignore = true)
    Passenger toPassenger(PassengerRequest passengerRequest);

    PassengerResponse toPassengerResponse(Passenger passenger);
}
