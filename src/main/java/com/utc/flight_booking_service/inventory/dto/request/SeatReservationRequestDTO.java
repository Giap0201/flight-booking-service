package com.utc.flight_booking_service.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatReservationRequestDTO {
    @NotNull(message = "FLIGHT_CLASS_ID_REQUIRED")
    UUID flightClassId;

    @Min(value = 1, message = "MIN_SEAT_RESERVATION")
    int amount;
}
