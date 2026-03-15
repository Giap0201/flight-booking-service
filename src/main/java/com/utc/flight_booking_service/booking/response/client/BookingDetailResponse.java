package com.utc.flight_booking_service.booking.response.client;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.response.share.ContactResponse;
import com.utc.flight_booking_service.payment.dto.response.ClientTransactionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDetailResponse {
    UUID id;
    String pnrCode;
    BookingStatus status;
    BigDecimal totalAmount;
    String currency;

    ContactResponse contact;
    List<PassengerTicketResponse> passengers;
    List<ClientTransactionResponse> transactions;
}
