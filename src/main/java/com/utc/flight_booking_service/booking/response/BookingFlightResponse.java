package com.utc.flight_booking_service.booking.response;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.FlightDirection;
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
