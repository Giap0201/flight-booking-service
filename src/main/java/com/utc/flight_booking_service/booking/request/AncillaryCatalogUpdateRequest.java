package com.utc.flight_booking_service.booking.request;

import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogType;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AncillaryCatalogUpdateRequest {
    AncillaryCatalogType type;

    String name;

    @Min(value = 0, message = "ANCILLARY_CATALOG_PRICE_INVALID")
    BigDecimal price;

    AncillaryCatalogStatus status;
}
