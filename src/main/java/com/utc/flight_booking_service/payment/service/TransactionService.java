package com.utc.flight_booking_service.payment.service;

import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    List<AdminTransactionResponse> getTransactionsByBookingId(UUID bookingId);
}
