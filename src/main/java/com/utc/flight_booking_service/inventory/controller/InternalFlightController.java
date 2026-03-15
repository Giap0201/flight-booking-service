package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.inventory.dto.request.FlightValidationRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.SeatReservationRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.SeatReservationResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
import com.utc.flight_booking_service.inventory.service.IFlightSearchService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/flights")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalFlightController {
    IFlightClassService flightClassService;
    IFlightSearchService flightSearchService;

    @PostMapping("/reserve-seats")
    public ApiResponse<SeatReservationResponseDTO> reserveSeats(@RequestBody @Valid SeatReservationRequestDTO request) {
        return ApiResponse.<SeatReservationResponseDTO>builder()
                .message("Giữ chỗ thành công")
                .result(flightClassService.decreaseSeats(request.getFlightClassId(), request.getAmount()))
                .build();
    }

    @PostMapping("/release-seats")
    public ApiResponse<SeatReservationResponseDTO> releaseSeats(@RequestBody @Valid SeatReservationRequestDTO request) {
        return ApiResponse.<SeatReservationResponseDTO>builder()
                .message("Hoàn ghế thành công")
                .result(flightClassService.increaseSeats(request.getFlightClassId(), request.getAmount()))
                .build();
    }

    @GetMapping("/{flightClassId}/price")
    public ApiResponse<FlightPriceResponseDTO> getFlightPrice(@PathVariable UUID flightClassId) {
        return ApiResponse.<FlightPriceResponseDTO>builder()
                .message("Lấy thông tin giá vé thành công")
                .result(flightClassService.getFlightPrice(flightClassId))
                .build();
    }

    @PostMapping("/validate")
    public ApiResponse<Boolean> validateFlight(@RequestBody FlightValidationRequestDTO request) {
        boolean isValid = flightSearchService.validateFlightForBooking(request);
        return ApiResponse.<Boolean>builder()
                .message(isValid ? "Chuyến bay hợp lệ" : "Thông tin chuyến bay đã thay đổi, vui lòng kiểm tra lại")
                .result(isValid)
                .build();
    }


    @GetMapping("/batch")
    public ApiResponse<List<FlightSearchResponseDTO>> getFlightsBatch(
            @RequestParam("ids") List<UUID> ids) {

        List<FlightSearchResponseDTO> result = flightSearchService.getFlightsByIds(ids);

        return ApiResponse.<List<FlightSearchResponseDTO>>builder()
                .message("Lấy thông tin danh sách chuyến bay thành công")
                .result(result)
                .build();
    }
}
