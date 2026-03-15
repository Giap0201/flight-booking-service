package com.utc.flight_booking_service.booking.controller;

import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.request.BookingSearchRequest;
import com.utc.flight_booking_service.booking.response.client.BookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.client.BookingCreatedResponse;
import com.utc.flight_booking_service.booking.response.client.BookingDetailResponse;
import com.utc.flight_booking_service.booking.response.share.ETicketEmailModel;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.booking.service.PdfGenerationService;
import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    BookingService bookingService;
    PdfGenerationService pdfGenerationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<BookingCreatedResponse> createBooking(@RequestBody @Valid BookingRequest bookingRequest) {
        return ApiResponse.<BookingCreatedResponse>builder()
                .result(bookingService.createBooking(bookingRequest))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<BookingDetailResponse> getBookingById(@PathVariable UUID id) {
        return ApiResponse.<BookingDetailResponse>builder()
                .result(bookingService.getBookingById(id))
                .build();
    }


    @GetMapping("/{bookingId}/tickets")
    ApiResponse<List<ETicketEmailModel>> getClientETickets(@PathVariable UUID bookingId) {
        return ApiResponse.<List<ETicketEmailModel>>builder()
                .result(bookingService.getTicketsByBookingId(bookingId))
                .build();
    }

    @GetMapping("/pnr/{pnrCode}/tickets")
    ApiResponse<List<ETicketEmailModel>> getClientETicketsByPnrCode(@PathVariable String pnrCode) {
        return ApiResponse.<List<ETicketEmailModel>>builder()
                .result(bookingService.getTicketsByPnrCode(pnrCode))
                .build();
    }

    @GetMapping("/search")
    ApiResponse<BookingDetailResponse> getBookingByPnrAndContactEmail(@Valid BookingSearchRequest request) {
        return ApiResponse.<BookingDetailResponse>builder()
                .result(bookingService.getBookingClientByPnrAndContactEmail(request))
                .build();
    }

    @GetMapping("/my-bookings")
    ApiResponse<PageResponse<BookingSummaryResponse>> getMyBookings(
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<BookingSummaryResponse>>builder()
                .result(bookingService.getMyBookings(filter, page, size))
                .build();
    }

    @PostMapping("/{id}/cancel")
    ApiResponse<String> cancelUnpaidBooking(@PathVariable UUID id) {
        bookingService.cancelUnpaidBooking(id);
        return ApiResponse.<String>builder()
                .message("Hủy vé thành công")
                .result("CANCELLED")
                .build();
    }

    @GetMapping("/{pnrCode}/download-pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable String pnrCode) {

        // 1. Dùng hàm CÓ SẴN của bạn để lấy ra cái List DTO y hệt như cục JSON bạn vừa gửi
        List<ETicketEmailModel> tickets = bookingService.getTicketsByPnrCode(pnrCode);

        byte[] pdfBytes = pdfGenerationService.generateTicketPdf(tickets);

        // 3. Gắn Header để báo cho trình duyệt biết đây là file PDF tải về
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "E-Ticket-" + pnrCode + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
