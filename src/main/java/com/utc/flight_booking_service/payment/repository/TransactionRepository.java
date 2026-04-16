package com.utc.flight_booking_service.payment.repository;

import com.utc.flight_booking_service.payment.entity.Transaction;
import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findAllByBookingId(UUID bookingId);

    boolean existsByBankRefNo(String bankRefNo);

    boolean existsByTransactionNo(String vnpBankTranNo);

    List<Transaction> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);

    Optional<Transaction> findByBookingIdAndStatusAndPaymentMethod(UUID id, PaymentStatus paymentStatus, String vnpay);
}
