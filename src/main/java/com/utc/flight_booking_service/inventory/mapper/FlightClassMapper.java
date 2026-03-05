package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.PriceUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.SeatReservationResponseDTO;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlightClassMapper {

    @Mapping(target = "flightClassId", source = "id")
    @Mapping(target = "newPrice", source = "basePrice")
    PriceUpdateResponseDTO toPriceResponse(FlightClass flightClass);

    @Mapping(target = "flightClassId", source = "id")
    @Mapping(target = "availableSeatsLeft", source = "availableSeats")
    @Mapping(target = "amountReserved", ignore = true) // Sẽ set thủ công trong Service
    SeatReservationResponseDTO toReservationResponse(FlightClass flightClass);

    @Mapping(target = "flightClassId", source = "id")
    @Mapping(target = "flightId", source = "flight.id")
    @Mapping(source = "flight.flightNumber", target = "flightNumber")
    @Mapping(source = "flight.departureTime", target = "departureTime")
    @Mapping(source = "flight.arrivalTime", target = "arrivalTime")
    @Mapping(source = "flight.origin.name", target = "origin")
    @Mapping(source = "flight.destination.name", target = "destination")
    FlightPriceResponseDTO toFlightPriceResponseDTO(FlightClass flightClass);
}
