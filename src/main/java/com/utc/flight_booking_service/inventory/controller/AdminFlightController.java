package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.inventory.dto.request.FlightUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.PriceUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.PriceUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/flights")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminFlightController {
    IFlightService flightService;

    // API Cập nhật giờ bay hoặc Hủy chuyến
    @PatchMapping("/{id}")
    public ApiResponse<FlightUpdateResponseDTO> updateFlight(@PathVariable String id, @RequestBody FlightUpdateRequestDTO request) {
        return ApiResponse.<FlightUpdateResponseDTO>builder()
                .message("Cập nhật thông tin chuyến bay thành công")
                .result(flightService.updateFlightStatus(id, request))
                .build();
    }

    @PutMapping("/prices/{flightClassId}")
    public ApiResponse<PriceUpdateResponseDTO> updatePrice(@PathVariable String flightClassId,
                                                           @RequestBody @Valid PriceUpdateRequestDTO request) {
        return ApiResponse.<PriceUpdateResponseDTO>builder()
                .message("Cập nhật giá vé thành công")
                .result(flightService.updatePrice(flightClassId, request))
                .build();
    }
}
