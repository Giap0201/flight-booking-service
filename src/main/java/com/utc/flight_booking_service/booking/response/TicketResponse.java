package com.utc.flight_booking_service.booking.response;

import com.utc.flight_booking_service.booking.enums.TicketStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketResponse {
    String ticketNumber;
    BigDecimal baseFare;
    BigDecimal taxAmount;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    String seatNumber;
    TicketStatus status;
    UUID passengerId;
    UUID flightId;
    UUID flightClassId;
}
