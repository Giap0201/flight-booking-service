package com.utc.flight_booking_service.booking.response.admin;


import com.utc.flight_booking_service.booking.enums.BookingStatus;
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
public class AdminBookingSummaryResponse {
    UUID id;
    String pnrCode;
    BookingStatus status;
    BigDecimal totalAmount;
    LocalDateTime createdAt;

    String flightNumber;
    String origin;
    String destination;
    LocalDateTime departureTime;

    String contactName;
    String contactPhone;
    String contactEmail;
}
