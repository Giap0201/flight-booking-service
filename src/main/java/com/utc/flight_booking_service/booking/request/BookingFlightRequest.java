package com.utc.flight_booking_service.booking.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingFlightRequest {
    @NotNull(message = "DIRECTION_REQUIRED")
    FlightDirection direction;

    @NotNull(message = "FLIGHT_ID_REQUIRED")
    UUID flightId;

    @NotNull(message = "FLIGHT_CLASS_ID_REQUIRED")
    UUID flightClassId;
}
