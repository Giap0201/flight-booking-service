package com.utc.flight_booking_service.booking.response;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    UUID id;
    String pnrCode;
    BigDecimal totalAmount;
    String currency;
    BookingStatus status;
    LocalDateTime expireAt;
    String contactName;
    String contactEmail;
    String contactPhone;
    BigDecimal totalFareAmount;
    BigDecimal totalTaxAmount;
    BigDecimal totalDiscountAmount;
    String promotionCode;
    List<BookingFlightResponse> flights;
    List<PassengerResponse> passengers;
    List<TicketResponse> tickets;
}
