package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.dto.request.FlightSearchRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.mapper.FlightSearchMapper;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import com.utc.flight_booking_service.inventory.repository.FlightSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightSearchService implements IFlightSearchService{
    FlightRepository flightRepository;
    FlightSearchMapper flightSearchMapper;

    @Cacheable(value = "flight_search",
            key = "'SEARCH:' + #request.origin + ':' + #request.destination + ':' + #request.date")
    public List<FlightSearchResponseDTO> searchAvailableFlights(FlightSearchRequestDTO request) {
        Specification<Flight> spec = FlightSpecification.searchFlights(
                request.getOrigin(),
                request.getDestination(),
                request.getDate(),
                request.getPassengers()
        );

        List<Flight> flights = flightRepository.findAll(spec);

        if (flights.isEmpty()) {
            throw new AppException(ErrorCode.FLIGHT_NOT_FOUND);
        }

        return flightSearchMapper.toResponseDTOList(flights);
    }
}
