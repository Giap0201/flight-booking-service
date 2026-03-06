package com.utc.flight_booking_service.booking.controller;

import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.request.BookingSearchRequest;
import com.utc.flight_booking_service.booking.response.BookingDetailsResponse;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import com.utc.flight_booking_service.booking.response.BookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.ClientETicketResponse;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<BookingResponse> createBooking(@RequestBody @Valid BookingRequest bookingRequest) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.createBooking(bookingRequest))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<BookingResponse> getBookingById(@PathVariable UUID id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.getBookingById(id))
                .build();
    }


    @GetMapping("/{bookingId}/tickets")
    ApiResponse<List<ClientETicketResponse>> getClientETickets(@PathVariable UUID bookingId) {
        return ApiResponse.<List<ClientETicketResponse>>builder()
                .result(bookingService.getTicketsByBookingId(bookingId))
                .build();
    }

    @GetMapping("/search")
    ApiResponse<BookingDetailsResponse> getBookingByPnrAndContactEmail(@Valid BookingSearchRequest request) {
        return ApiResponse.<BookingDetailsResponse>builder()
                .result(bookingService.getBookingClientByPnrAndContactEmail(request))
                .build();
    }

    @GetMapping("/my-bookings")
    ApiResponse<Page<BookingSummaryResponse>> getMyBookings(
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<BookingSummaryResponse>>builder()
                .result(bookingService.getMyBookings(filter, page, size))
                .build();
    }
}
