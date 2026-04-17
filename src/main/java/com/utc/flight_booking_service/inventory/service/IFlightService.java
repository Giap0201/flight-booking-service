package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.request.FlightManualRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.FlightUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.PriceUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightStatisticsResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.PriceUpdateResponseDTO;

import java.util.UUID;

public interface IFlightService {
    FlightUpdateResponseDTO updateFlightStatus(UUID flightId, FlightUpdateRequestDTO request);

    PriceUpdateResponseDTO updatePrice(UUID flightClassId, PriceUpdateRequestDTO request);

    UUID createManualFlight(FlightManualRequestDTO request);

    FlightStatisticsResponseDTO getTodayStatistics();

    PageResponse<FlightSearchResponseDTO> getAllFlights(int page, int size, String sortBy, String sortDir);

    PageResponse<FlightSearchResponseDTO> searchFlightsForAdmin(
            String flightNumber, String airlineCode, String originCode, String destinationCode,
            int page, int size, String sortBy, String sortDir);
}
