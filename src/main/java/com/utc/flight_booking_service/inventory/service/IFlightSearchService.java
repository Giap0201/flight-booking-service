package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.request.FlightSearchRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.FlightValidationRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.CheapestDateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightDetailResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IFlightSearchService {
    List<FlightSearchResponseDTO> searchAvailableFlights(FlightSearchRequestDTO request);

    FlightDetailResponseDTO getFlightDetail(UUID id);

    boolean validateFlightForBooking(FlightValidationRequestDTO request);

    List<FlightSearchResponseDTO> getFlightsByIds(List<UUID> ids);

    List<CheapestDateResponseDTO> getCheapestPricesInMonth(String origin, String destination, int year, int month);
}
