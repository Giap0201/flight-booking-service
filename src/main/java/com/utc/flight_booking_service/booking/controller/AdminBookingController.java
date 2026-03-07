package com.utc.flight_booking_service.booking.controller;


import com.utc.flight_booking_service.booking.request.AdminBookingSearchRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.client.BookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.share.PageResponse;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.common.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    BookingService bookingService;

    @GetMapping
    public ApiResponse<PageResponse<AdminBookingSummaryResponse>> searchBookings(AdminBookingSearchRequest request,
                                                                            @RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<AdminBookingSummaryResponse>>builder()
                .result(bookingService.searchBookingsForAdmin(request, page, size))
                .build();
    }
}