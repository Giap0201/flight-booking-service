package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
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
public class FlightClassService {
    FlightClassRepository flightClassRepository;

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public void decreaseSeats(Long flightClassId, int amount) {
        FlightClass flightClass = flightClassRepository.findById(flightClassId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        if (flightClass.getAvailableSeats() < amount) {
            throw new AppException(ErrorCode.NOT_ENOUGH_SEATS);
        }

        flightClass.setAvailableSeats(flightClass.getAvailableSeats() - amount);

        try {
            flightClassRepository.saveAndFlush(flightClass);
        } catch (OptimisticLockingFailureException e) {
            // log.error("Concurrency conflict for flightClassId: {}", flightClassId);
            throw new AppException(ErrorCode.UPDATE_SEAT_FAILED);
        }
    }
}
