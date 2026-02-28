package com.utc.flight_booking_service.inventory.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceUpdateRequestDTO {
    @Min(value = 0, message = "INVALID_PRICE")
    BigDecimal basePrice;
}
