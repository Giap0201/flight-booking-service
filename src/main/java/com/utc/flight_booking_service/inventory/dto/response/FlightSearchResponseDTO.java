package com.utc.flight_booking_service.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightSearchResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    UUID id;
    String flightNumber;
    String airlineName;
    String origin;
    String destination;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    String status;
    List<FlightClassDTO> classes;
}
