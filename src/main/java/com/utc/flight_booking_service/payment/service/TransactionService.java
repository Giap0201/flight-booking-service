package com.utc.flight_booking_service.payment.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.payment.dto.request.TransactionSearchRequest;
import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import com.utc.flight_booking_service.payment.dto.response.ClientTransactionResponse;
import com.utc.flight_booking_service.payment.entity.Transaction;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    List<AdminTransactionResponse> getTransactionsByBookingId(UUID bookingId);

    PageResponse<AdminTransactionResponse> getAllTransactions(@ModelAttribute TransactionSearchRequest request, int page, int size);

    // Khach hang xem lai lich su thanh toan
    List<ClientTransactionResponse> getClientTransactionsByBookingId(UUID bookingId);

    Transaction findSuccessfulVnpayTransactionByBookingId(UUID bookingId);

}
