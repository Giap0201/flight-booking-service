package com.utc.flight_booking_service.booking.controller;


import com.utc.flight_booking_service.booking.request.AdminBookingSearchRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingDetailResponse;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingSummaryResponse;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    BookingService bookingService;

    @GetMapping
    public ApiResponse<PageResponse<AdminBookingSummaryResponse>> searchBookings(@ModelAttribute AdminBookingSearchRequest request,
                                                                                 @RequestParam(defaultValue = "1") int page,
                                                                                 @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<AdminBookingSummaryResponse>>builder()
                .result(bookingService.searchBookingsForAdmin(request, page, size))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminBookingDetailResponse> getBookingDetailsForAdmin(@PathVariable UUID id) {
        return ApiResponse.<AdminBookingDetailResponse>builder()
                .result(bookingService.getBookingDetailsForAdmin(id))
                .build();
    }
}