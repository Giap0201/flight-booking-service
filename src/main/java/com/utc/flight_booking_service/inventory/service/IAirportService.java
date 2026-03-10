package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;

import java.util.List;

public interface IAirportService {
    List<AirportResponseDTO> getAllAirports(String keyword);
}
