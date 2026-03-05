package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.inventory.dto.request.SeatReservationRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.SeatReservationResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/flights")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalFlightController {
    IFlightClassService flightClassService;

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
}
