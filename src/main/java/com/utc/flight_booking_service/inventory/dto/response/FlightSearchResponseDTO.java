package com.utc.flight_booking_service.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightSearchResponseDTO {
    String flightNumber;
    String airlineName;
    String origin;
    String destination;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    String status;
    List<FlightClassDTO> classes;
}
