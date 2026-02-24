package com.utc.flight_booking_service.booking.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingFlightResponse {
    UUID id;
    BigDecimal priceAtBooking;
    FlightDirection direction;
    UUID flightId;
    UUID flightClassId;
}
