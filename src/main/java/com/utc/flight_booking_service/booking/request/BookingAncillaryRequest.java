package com.utc.flight_booking_service.booking.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingAncillaryRequest {
    @NotNull(message = "ANCILLARY_CATALOG_ID_REQUIRED")
    UUID catalogId;

    @NotNull(message = "PASSENGER_INDEX_REQUIRED")
    Integer passengerIndex;

    @NotNull(message = "SEGMENT_NO_REQUIRED")
    Integer segmentNo;
}
