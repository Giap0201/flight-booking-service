package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.SeatReservationResponseDTO;

import java.util.UUID;

public interface IFlightClassService {
    SeatReservationResponseDTO decreaseSeats(UUID flightClassId, int amount);
    SeatReservationResponseDTO increaseSeats(UUID flightClassId, int amount);
    FlightPriceResponseDTO getFlightPrice(UUID flightClassId);
}
