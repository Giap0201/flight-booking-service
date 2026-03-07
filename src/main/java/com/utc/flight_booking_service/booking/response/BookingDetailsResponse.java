package com.utc.flight_booking_service.booking.response;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
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
public class BookingDetailsResponse {
    String pnrCode;
    BookingStatus status;
    BigDecimal grandTotal;
    LocalDateTime expireAt;
    List<ClientETicketResponse> tickets;
}
