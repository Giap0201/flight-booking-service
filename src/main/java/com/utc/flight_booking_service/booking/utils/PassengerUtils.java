package com.utc.flight_booking_service.booking.utils;

import com.utc.flight_booking_service.booking.enums.PassengerType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class PassengerUtils {
    public static PassengerType calculatePassengerType(LocalDate dateOfBirth, LocalDateTime departureDateTime) {
        if (dateOfBirth == null || departureDateTime == null) return PassengerType.ADULT;
        LocalDate departureDate = departureDateTime.toLocalDate();
        int age = Period.between(dateOfBirth, departureDate).getYears();
        if (age < 2) return PassengerType.INFANT;
        if (age < 12) return PassengerType.CHILD;
        return PassengerType.ADULT;
    }
}
