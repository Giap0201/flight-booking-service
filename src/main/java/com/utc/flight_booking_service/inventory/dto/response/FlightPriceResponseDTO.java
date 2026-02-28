package com.utc.flight_booking_service.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightPriceResponseDTO {
    String flightClassId;
    String flightId;
    String classType;
    BigDecimal basePrice;
    Double taxPercentage;
}
