package com.utc.flight_booking_service.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightDetailResponseDTO {
    UUID id;
    String flightNumber;
    String status;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;

    AirlineResponseDTO airline;
    AirportResponseDTO origin;
    AirportResponseDTO destination;
    AircraftResponseDTO aircraft;
    List<FlightClassResponseDTO> flightClasses;
}
