package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.response.AirlineResponseDTO;

public interface IAirlineService {
    PageResponse<AirlineResponseDTO> getAllAirlines(int page, int size, String sortBy, String sortDir);
}
