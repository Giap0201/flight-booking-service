package com.utc.flight_booking_service.booking.response.share;

import com.utc.flight_booking_service.booking.enums.PassengerType;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.inventory.entity.FlightClassType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ETicketEmailModel {
    String ticketNumber;
    String pnrCode;
    TicketStatus status;

    String passengerFullName;
    PassengerType passengerType;

    String flightNumber;
    String origin;
    String destination;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;

    FlightClassType classType;
    String seatNumber;
    String baggageAllowance;

    BigDecimal totalAmount;
}