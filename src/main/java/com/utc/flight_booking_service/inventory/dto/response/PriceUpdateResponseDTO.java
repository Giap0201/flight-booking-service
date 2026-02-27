package com.utc.flight_booking_service.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceUpdateResponseDTO {
    Long flightClassId;
    Double newPrice;
    LocalDateTime updatedAt;
}
