package com.utc.flight_booking_service.inventory.dto.response;

import com.utc.flight_booking_service.inventory.entity.FlightClassType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightPriceResponseDTO {
    String flightClassId;
    FlightClassType classType;
    String flightId;
    BigDecimal basePrice;
    Double taxPercentage;
    String flightNumber;
    String origin;
    String destination;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
}
