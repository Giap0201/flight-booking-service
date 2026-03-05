package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.request.FlightSearchRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;

import java.util.List;

public interface IFlightSearchService {
    List<FlightSearchResponseDTO> searchAvailableFlights(FlightSearchRequestDTO request);
}
