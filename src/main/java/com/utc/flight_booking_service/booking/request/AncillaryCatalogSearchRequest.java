package com.utc.flight_booking_service.booking.request;

import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AncillaryCatalogSearchRequest {
    String keyword;
    AncillaryCatalogType type;
    AncillaryCatalogStatus status;
}
