package com.utc.flight_booking_service.payment.repository;

import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import com.utc.flight_booking_service.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByBookingId(UUID bookingId);

    boolean existsByBankRefNo(String bankRefNo);

    boolean existsByTransactionNo(String vnpBankTranNo);
}
