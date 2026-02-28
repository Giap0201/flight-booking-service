package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.request.FlightUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.PriceUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.PriceUpdateResponseDTO;

public interface IFlightService {
    FlightUpdateResponseDTO updateFlightStatus(String flightId, FlightUpdateRequestDTO request);
    PriceUpdateResponseDTO updatePrice(String flightClassId, PriceUpdateRequestDTO request);
}
