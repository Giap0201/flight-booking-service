package com.utc.flight_booking_service.booking.response.client;

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
public class BookingCreatedResponse {
    UUID id;
    String pnrCode;
    BigDecimal totalAmount;
    String currency;
    BookingStatus status;
    LocalDateTime expireAt;
}
