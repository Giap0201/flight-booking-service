package com.utc.flight_booking_service.booking.controller;


import com.utc.flight_booking_service.booking.request.AdminBookingSearchRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingDetailResponse;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingSummaryResponse;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.payment.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    BookingService bookingService;
    PaymentService paymentService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<AdminBookingSummaryResponse>> searchBookings(@ModelAttribute AdminBookingSearchRequest request,
                                                                                 @RequestParam(defaultValue = "1") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<AdminBookingSummaryResponse>>builder()
                .result(bookingService.searchBookingsForAdmin(request, page, size))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{bookingId}")
    public ApiResponse<AdminBookingDetailResponse> getBookingDetailsForAdmin(@PathVariable UUID bookingId) {
        return ApiResponse.<AdminBookingDetailResponse>builder()
                .result(bookingService.getBookingDetailsForAdmin(bookingId))
                .build();
    }

    // Admin huy ve cho khach va hoan tien
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Void>> forceCancelPaidBooking(@PathVariable UUID bookingId) {
        bookingService.forceCancelAndRefund(bookingId);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    // Kiem tra lai trang thai thanh toan cho khach hang
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/verify-payment/{pnrCode}")
    public ApiResponse<Map<String, String>> verifyPaymentStatusImmediately(@PathVariable String pnrCode) {
        Map<String, String> result = paymentService.verifyPaymentStatusImmediately(pnrCode);
        return ApiResponse.<Map<String, String>>builder()
                .result(result)
                .build();
    }

    // Gui mail lai cho khach hang
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{bookingId}/resend-email")
    public ResponseEntity<ApiResponse<Void>> resendBookingEmail(@PathVariable UUID bookingId) {
        bookingService.resendBookingEmail(bookingId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .build();
        return ResponseEntity.ok(response);
    }

}