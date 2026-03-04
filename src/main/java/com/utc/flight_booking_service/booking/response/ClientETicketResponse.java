package com.utc.flight_booking_service.booking.response;


import com.utc.flight_booking_service.booking.enums.PassengerType;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientETicketResponse {
    String ticketNumber;
    String pnrCode;
    TicketStatus status;

    String passengerFullName;
    PassengerType passengerType;

    String flightNumber;
    String origin;
    String destination;
    LocalDateTime departureTime;

    BigDecimal totalAmount;
}