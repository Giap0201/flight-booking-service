package com.utc.flight_booking_service.payment.controller;


import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.payment.dto.request.TransactionSearchRequest;
import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import com.utc.flight_booking_service.payment.service.TransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {
    TransactionService transactionService;

    @GetMapping
    ApiResponse<PageResponse<AdminTransactionResponse>> getAllTransactions(
            @ModelAttribute TransactionSearchRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<AdminTransactionResponse>>builder()
                .result(transactionService.getAllTransactions(request, page, size))
                .build();
    }
}
