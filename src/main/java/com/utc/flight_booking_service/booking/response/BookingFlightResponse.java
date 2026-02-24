package com.utc.flight_booking_service.booking.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingFlightResponse {
    UUID id;
    UUID flightId;
    UUID flightClassId;
    int segmentNo;
    String originFlightNumber;
    LocalDateTime originDepartureTime;
    LocalDateTime originArrivalTime;
}
