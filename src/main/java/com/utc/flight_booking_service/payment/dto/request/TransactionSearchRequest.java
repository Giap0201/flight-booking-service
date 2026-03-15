package com.utc.flight_booking_service.payment.dto.request;

import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionSearchRequest {
    String keyword;
    PaymentStatus status;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
