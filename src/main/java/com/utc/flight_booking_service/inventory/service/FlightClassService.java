package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.SeatReservationResponseDTO;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import com.utc.flight_booking_service.inventory.mapper.FlightClassMapper;
import com.utc.flight_booking_service.inventory.repository.FlightClassRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightClassService implements IFlightClassService{
    FlightClassRepository flightClassRepository;
    FlightClassMapper flightClassMapper;

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public SeatReservationResponseDTO decreaseSeats(String flightClassId, int amount) {
        FlightClass flightClass = flightClassRepository.findById(flightClassId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        if (flightClass.getAvailableSeats() < amount) {
            throw new AppException(ErrorCode.NOT_ENOUGH_SEATS);
        }

        flightClass.setAvailableSeats(flightClass.getAvailableSeats() - amount);

        try {
            FlightClass saved = flightClassRepository.saveAndFlush(flightClass);
            SeatReservationResponseDTO response = flightClassMapper.toReservationResponse(saved);
            response.setAmountReserved(amount);
            return response;
        } catch (OptimisticLockingFailureException e) {
            throw new AppException(ErrorCode.UPDATE_SEAT_FAILED);
        }
    }

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public SeatReservationResponseDTO increaseSeats(String flightClassId, int amount) {
        FlightClass flightClass = flightClassRepository.findById(flightClassId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        flightClass.setAvailableSeats(flightClass.getAvailableSeats() + amount);

        try {
            FlightClass saved = flightClassRepository.saveAndFlush(flightClass);
            SeatReservationResponseDTO response = flightClassMapper.toReservationResponse(saved);
            response.setAmountReserved(amount);
            return response;
        } catch (OptimisticLockingFailureException e) {
            throw new AppException(ErrorCode.UPDATE_SEAT_FAILED);
        }
    }

    @Override
    public FlightPriceResponseDTO getFlightPrice(String flightClassId) {
        FlightClass flightClass = flightClassRepository.findById(flightClassId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        return flightClassMapper.toFlightPriceResponseDTO(flightClass);
    }
}
