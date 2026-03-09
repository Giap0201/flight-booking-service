package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.response.AirlineResponseDTO;

import java.util.List;

public interface IAirlineService {
    List<AirlineResponseDTO> getAllAirlines();
}
