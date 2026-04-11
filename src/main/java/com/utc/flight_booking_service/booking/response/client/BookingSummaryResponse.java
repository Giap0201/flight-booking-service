package com.utc.flight_booking_service.booking.response.client;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.inventory.entity.FlightClassType;
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
//Dung de tra ve khi xem lich su dat ve
public class BookingSummaryResponse {
    UUID id;
    String pnrCode;
    BookingStatus status;
    BigDecimal totalAmount;
    LocalDateTime createdAt;
    String flightNumber;
    String origin;
    String destination;
    LocalDateTime departureTime;

    //Trung them
    LocalDateTime arrivalTime;       // NEW: Arrival time of the flight
    FlightClassType classType;       // NEW: Class type (ECONOMY, BUSINESS, etc.)
    Integer passengerCount;          // NEW: Number of passengers in booking

}
