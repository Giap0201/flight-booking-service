package com.utc.flight_booking_service.booking;

import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import com.utc.flight_booking_service.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping({"/{id}"})
    ApiResponse<BookingResponse> getBookingById(@PathVariable UUID id) {
        return ApiResponse.<BookingResponse>builder()
                .result(bookingService.getBookingById(id))
                .build();
    }

}
