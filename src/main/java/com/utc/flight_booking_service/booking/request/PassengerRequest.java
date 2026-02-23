package com.utc.flight_booking_service.booking.request;

import com.utc.flight_booking_service.booking.enums.Gender;
import com.utc.flight_booking_service.booking.enums.PassengerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PassengerRequest {
    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @Size(max = 50, message = "FIRST_NAME_TOO_LONG")
    String firstName;

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @Size(max = 50, message = "LAST_NAME_TOO_LONG")
    String lastName;

    @NotNull(message = "DOB_REQUIRED")
    @Past(message = "DOB_MUST_BE_IN_PAST")
    LocalDate dateOfBirth;

    @NotNull(message = "GENDER_REQUIRED")
    Gender gender;

    @NotNull(message = "PASSENGER_TYPE_REQUIRED")
    PassengerType type;
}
