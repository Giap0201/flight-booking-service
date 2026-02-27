package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.dto.request.FlightUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.PriceUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.PriceUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import com.utc.flight_booking_service.inventory.mapper.FlightClassMapper;
import com.utc.flight_booking_service.inventory.mapper.FlightMapper;
import com.utc.flight_booking_service.inventory.repository.FlightClassRepository;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightService {
    FlightRepository flightRepository;
    FlightClassRepository flightClassRepository;
    FlightMapper flightMapper;
    FlightClassMapper flightClassMapper;

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public FlightUpdateResponseDTO updateFlightStatus(Long flightId, FlightUpdateRequestDTO request) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        if (request.getDepartureTime() != null) flight.setDepartureTime(request.getDepartureTime());
        if (request.getArrivalTime() != null) flight.setArrivalTime(request.getArrivalTime());
        if (request.getStatus() != null) flight.setStatus(request.getStatus());

        return flightMapper.toUpdateResponse(flightRepository.save(flight));
    }

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public PriceUpdateResponseDTO updatePrice(Long flightClassId, PriceUpdateRequestDTO request) {
        FlightClass flightClass = flightClassRepository.findById(flightClassId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        flightClass.setBasePrice(request.getBasePrice());
        return flightClassMapper.toPriceResponse(flightClassRepository.save(flightClass));
    }
}
