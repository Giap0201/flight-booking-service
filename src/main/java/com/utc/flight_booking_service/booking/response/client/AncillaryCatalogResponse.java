package com.utc.flight_booking_service.booking.response.client;

import com.utc.flight_booking_service.booking.enums.AncillaryCatalogType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AncillaryCatalogResponse {
    UUID id;
    String code;
    AncillaryCatalogType type;
    String name;
    BigDecimal price;
}
