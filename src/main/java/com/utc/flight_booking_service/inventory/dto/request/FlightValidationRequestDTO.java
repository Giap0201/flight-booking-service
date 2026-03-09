package com.utc.flight_booking_service.inventory.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightValidationRequestDTO {
    UUID flightId;
    UUID flightClassId;
    BigDecimal expectedPrice;
    int numberOfPassengers;
}
