package com.utc.flight_booking_service.inventory.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightManualRequestDTO {
    String flightNumber;
    String airlineCode;
    String aircraftCode;
    String originCode;
    String destinationCode;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
}