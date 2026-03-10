package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;
import com.utc.flight_booking_service.inventory.mapper.AirportMapper;
import com.utc.flight_booking_service.inventory.repository.AirportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirportService implements IAirportService {
    AirportRepository airportRepository;
    AirportMapper airportMapper;

    @Override
    public List<AirportResponseDTO> getAllAirports(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return airportMapper.toResponseDTOList(airportRepository.findAll());
        }
        return airportMapper.toResponseDTOList(airportRepository.searchAirports(keyword));
    }
}
