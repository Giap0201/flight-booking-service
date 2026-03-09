package com.utc.flight_booking_service.payment.service;

import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import com.utc.flight_booking_service.payment.mapper.TransactionMapper;
import com.utc.flight_booking_service.payment.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionServiceImpl implements TransactionService {
    TransactionRepository transactionRepository;
    TransactionMapper transactionMapper;

    @Override
    public List<AdminTransactionResponse> getTransactionsByBookingId(UUID bookingId) {
        return transactionRepository.findAllByBookingId(bookingId).stream()
                .map(transactionMapper::toAdminTransactionResponse)
                .toList();
    }

}
