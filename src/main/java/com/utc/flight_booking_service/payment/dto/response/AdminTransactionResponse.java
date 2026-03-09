package com.utc.flight_booking_service.payment.dto.response;

import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminTransactionResponse {
    BigDecimal amount;
    String paymentMethod;
    PaymentStatus status;
    String transactionNo;
    String bankRefNo;
    String gatewayResponse;
    LocalDateTime createdAt;
}