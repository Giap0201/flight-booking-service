package com.utc.flight_booking_service.booking.response.admin;


import com.utc.flight_booking_service.booking.response.client.BookingDetailResponse;
import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminBookingDetailResponse {
    BookingDetailResponse baseDetails;

    UUID userId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    List<AdminTransactionResponse> transactions;
}
