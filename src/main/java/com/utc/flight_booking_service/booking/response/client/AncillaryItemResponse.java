package com.utc.flight_booking_service.booking.response.client;

import com.utc.flight_booking_service.booking.enums.AncillaryCatalogType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AncillaryItemResponse {
    String catalogName;
    AncillaryCatalogType type;
    BigDecimal amount;
}