package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.dto.response.AirlineResponseDTO;
import com.utc.flight_booking_service.inventory.mapper.AirlineMapper;
import com.utc.flight_booking_service.inventory.repository.AirlineRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirlineService implements IAirlineService {
    AirlineRepository airlineRepository;
    AirlineMapper airlineMapper;

    @Override
    public List<AirlineResponseDTO> getAllAirlines() {
        return airlineMapper.toResponseDTOList(airlineRepository.findAll());
    }
}
