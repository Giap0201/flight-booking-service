package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.SeatReservationResponseDTO;

public interface IFlightClassService {
    SeatReservationResponseDTO decreaseSeats(String flightClassId, int amount);
    SeatReservationResponseDTO increaseSeats(String flightClassId, int amount);
    FlightPriceResponseDTO getFlightPrice(String flightClassId);
}
