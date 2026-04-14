package com.utc.flight_booking_service.payment.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayController {

    PaymentService paymentService;

    // 1. Thêm tham số platform vào API tạo URL
    @GetMapping("/create-url")
    public ApiResponse<String> createPaymentUrl(
            @RequestParam UUID bookingId,
            @RequestParam(defaultValue = "web") String platform, // <-- THÊM DÒNG NÀY
            HttpServletRequest request) {

        // Gợi ý: Bạn cần truyền biến platform này vào PaymentService
        // để nối nó vào cái vnp_ReturnUrl. Ví dụ vnp_ReturnUrl sẽ thành:
        // http://localhost:8080/api/payments/vnpay-return?platform=android
        String paymentUrl = paymentService.createPaymentUrl(bookingId, platform, request);

        return ApiResponse.<String>builder()
                .result(paymentUrl)
                .build();
    }

    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Lấy link đích trực tiếp từ Service (đã bao gồm cả logic web/app)
        String redirectUrl = paymentService.processReturn(request);

        // Chuyển hướng luôn, không cần if/else gì ở đây nữa
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(HttpServletRequest request) {
        Map<String, String> result = paymentService.processIpn(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/verify-status/{pnrCode}")
    public ApiResponse<Map<String, String>> verifyPaymentStatusImmediately(@PathVariable String pnrCode) {
        Map<String, String> result = paymentService.verifyPaymentStatusImmediately(pnrCode);
        return ApiResponse.<Map<String, String>>builder()
                .result(result)
                .build();
    }
}