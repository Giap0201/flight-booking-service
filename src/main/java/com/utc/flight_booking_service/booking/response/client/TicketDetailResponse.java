package com.utc.flight_booking_service.booking.response.client;

import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.inventory.entity.FlightClassType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketDetailResponse {
    String ticketNumber;
    TicketStatus status;
    String seatNumber;
    BigDecimal totalAmount;
    String flightNumber;
    String departureAirport;
    String arrivalAirport;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    FlightClassType classType;
    List<AncillaryItemResponse> ancillaries;
}
