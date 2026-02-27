package com.utc.flight_booking_service.booking.response;

import com.utc.flight_booking_service.booking.enums.Gender;
import com.utc.flight_booking_service.booking.enums.PassengerType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PassengerResponse {
    UUID id;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Gender gender;
    PassengerType type;
}

