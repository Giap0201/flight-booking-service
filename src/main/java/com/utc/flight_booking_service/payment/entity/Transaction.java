package com.utc.flight_booking_service.payment.entity;

import com.utc.flight_booking_service.common.BaseEntity;
import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "transactions")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction extends BaseEntity {
    @Column(name = "booking_id", nullable = false)
    UUID bookingId;

    @Column(precision = 15, scale = 2)
    BigDecimal amount;

    @Column(name = "payment_method")
    String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    PaymentStatus status;

    @Column(name = "transaction_no")
    String transactionNo;

    @Column(name = "bank_ref_no")
    String bankRefNo;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    String gatewayResponse;
}