package com.utc.flight_booking_service.payment.repository;

import com.utc.flight_booking_service.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
