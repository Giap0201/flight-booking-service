package com.utc.flight_booking_service.payment.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.payment.dto.request.TransactionSearchRequest;
import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import com.utc.flight_booking_service.payment.dto.response.ClientTransactionResponse;
import com.utc.flight_booking_service.payment.entity.Transaction;
import com.utc.flight_booking_service.payment.mapper.TransactionMapper;
import com.utc.flight_booking_service.payment.repository.TransactionRepository;
import com.utc.flight_booking_service.payment.specification.TransactionSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public PageResponse<AdminTransactionResponse> getAllTransactions(TransactionSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<Transaction> spec = TransactionSpecification.getTransactionSpecification(request);
        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        List<AdminTransactionResponse> data = transactionMapper.toAdminTransactionResponseList(transactions.getContent());
        return PageResponse.<AdminTransactionResponse>builder()
                .data(data)
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .pageSize(transactions.getSize())
                .currentPage(page)
                .build();
    }

    @Override
    public List<ClientTransactionResponse> getClientTransactionsByBookingId(UUID bookingId) {
        List<Transaction> transactions = transactionRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }
        return transactionMapper.toClientTransactionResponseList(transactions);
    }

}
