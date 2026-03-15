package com.utc.flight_booking_service.inventory.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightUpdateRequestDTO {
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    String status; // SCHEDULED, DELAYED, CANCELLED
}
