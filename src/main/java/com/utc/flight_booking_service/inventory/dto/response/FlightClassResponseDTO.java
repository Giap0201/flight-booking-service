package com.utc.flight_booking_service.inventory.dto.response;


import com.utc.flight_booking_service.inventory.entity.FlightClassType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightClassResponseDTO {
    UUID id;
    FlightClassType classType; // 4 hạng vé: ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST_CLASS
    BigDecimal basePrice;
    Double taxPercentage;
    Integer availableSeats;
}
