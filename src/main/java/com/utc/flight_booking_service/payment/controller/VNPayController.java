package com.utc.flight_booking_service.payment.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class VNPayController {
    PaymentService paymentService;
    @GetMapping("/create-url")
    public ApiResponse<String> createPaymentUrl(
            @RequestParam UUID bookingId,
            HttpServletRequest request) {
        String paymentUrl = paymentService.createPaymentUrl(bookingId, request);
        return ApiResponse.<String>builder()
                .result(paymentUrl)
                .build();
    }
}
