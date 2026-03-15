package com.utc.flight_booking_service.payment.mapper;

import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import com.utc.flight_booking_service.payment.dto.response.ClientTransactionResponse;
import com.utc.flight_booking_service.payment.entity.Transaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    AdminTransactionResponse toAdminTransactionResponse(Transaction transaction);
    List<AdminTransactionResponse> toAdminTransactionResponseList(List<Transaction> transactions);
    ClientTransactionResponse toClientTransactionResponse(Transaction transaction);
    List<ClientTransactionResponse> toClientTransactionResponseList(List<Transaction> transactions);
}
