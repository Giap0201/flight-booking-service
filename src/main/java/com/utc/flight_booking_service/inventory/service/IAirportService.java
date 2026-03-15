package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;

public interface IAirportService {
    PageResponse<AirportResponseDTO> getAllAirports(String keyword, int page, int size, String sortBy, String sortDir);
}
