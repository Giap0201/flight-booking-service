package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import com.utc.flight_booking_service.inventory.repository.FlightClassRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightClassService {
    FlightClassRepository flightClassRepository;

    @Transactional
    public void decreaseSeats(Long flightClassId, int amount) {
        //Tìm hạng ghế
        FlightClass flightClass = flightClassRepository.findById(flightClassId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        if (flightClass.getAvailableSeats() < amount) {
            throw new AppException(ErrorCode.NOT_ENOUGH_SEATS);
        }

        flightClass.setAvailableSeats(flightClass.getAvailableSeats() - amount);

        try {
            //Lưu lại. Hibernate sẽ so sánh field 'version' tự động
            flightClassRepository.saveAndFlush(flightClass);
        } catch (OptimisticLockingFailureException e) {
            // THẤT BẠI: Nếu version trong DB khác với version lúc select lên
            // Ném lỗi về để module Booking biết và báo khách đặt lại
            throw new AppException(ErrorCode.UPDATE_SEAT_FAILED);
        }
    }
}
