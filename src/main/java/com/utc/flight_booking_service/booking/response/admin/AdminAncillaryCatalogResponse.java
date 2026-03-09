package com.utc.flight_booking_service.booking.response.admin;


import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAncillaryCatalogResponse {
    UUID id;
    String code;
    AncillaryCatalogType type;
    String name;
    BigDecimal price;
    AncillaryCatalogStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
