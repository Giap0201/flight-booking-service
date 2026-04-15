package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.request.AirportRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;

public interface IAirportService {
    PageResponse<AirportResponseDTO> getAllAirports(String keyword, int page, int size, String sortBy, String sortDir);

    AirportResponseDTO createAirport(AirportRequestDTO request);

    AirportResponseDTO updateAirport(String code, AirportRequestDTO request);

    void deleteAirport(String code);
}
