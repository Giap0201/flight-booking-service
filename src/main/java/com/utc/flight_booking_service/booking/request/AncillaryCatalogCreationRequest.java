package com.utc.flight_booking_service.booking.request;

import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AncillaryCatalogCreationRequest {
    @NotBlank(message = "ANCILLARY_CATALOG_CODE_REQUIRED")
    String code;

    @NotNull(message = "ANCILLARY_CATALOG_TYPE_REQUIRED")
    AncillaryCatalogType type;

    @NotBlank(message = "ANCILLARY_CATALOG_NAME_REQUIRED")
    String name;

    @NotNull(message = "ANCILLARY_CATALOG_PRICE_REQUIRED")
    @Min(value = 0, message = "ANCILLARY_CATALOG_PRICE_INVALID")
    BigDecimal price;

    AncillaryCatalogStatus status;
}