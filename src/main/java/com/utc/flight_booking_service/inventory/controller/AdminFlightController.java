package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.AppConstants;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.request.FlightManualRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.FlightUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.PriceUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightStatisticsResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.PriceUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightService;
import com.utc.flight_booking_service.inventory.service.IFlightSyncService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/flights")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminFlightController {
    IFlightService flightService;
    IFlightSyncService flightSyncService;

    // API Cập nhật giờ bay hoặc Hủy chuyến
    @PatchMapping("/{id}")
    public ApiResponse<FlightUpdateResponseDTO> updateFlight(@PathVariable UUID id, @RequestBody FlightUpdateRequestDTO request) {
        return ApiResponse.<FlightUpdateResponseDTO>builder()
                .message("Cập nhật thông tin chuyến bay thành công")
                .result(flightService.updateFlightStatus(id, request))
                .build();
    }

    @PutMapping("/prices/{flightClassId}")
    public ApiResponse<PriceUpdateResponseDTO> updatePrice(@PathVariable UUID flightClassId,
                                                           @RequestBody @Valid PriceUpdateRequestDTO request) {
        return ApiResponse.<PriceUpdateResponseDTO>builder()
                .message("Cập nhật giá vé thành công")
                .result(flightService.updatePrice(flightClassId, request))
                .build();
    }

    @PostMapping("/sync-now")
    public ApiResponse<String> triggerSync() {
        long startTime = System.currentTimeMillis();
        String resultSummary = flightSyncService.fetchAndMapFlights();

        long duration = System.currentTimeMillis() - startTime;

        return ApiResponse.<String>builder()
                .message("Kích hoạt đồng bộ thủ công thành công")
                .result(resultSummary + ". Thời gian thực hiện: " + duration + "ms")
                .build();
    }

    @PostMapping
    public ApiResponse<UUID> createManualFlight(@RequestBody FlightManualRequestDTO request) {
        UUID newFlightId = flightService.createManualFlight(request);

        return ApiResponse.<UUID>builder()
                .message("Tạo chuyến bay thành công")
                .result(newFlightId)
                .build();
    }

    @GetMapping("/statistics")
    public ApiResponse<FlightStatisticsResponseDTO> getStatistics() {
        return ApiResponse.<FlightStatisticsResponseDTO>builder()
                .message("Lấy thống kê tồn kho thành công")
                .result(flightService.getTodayStatistics())
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<FlightSearchResponseDTO>> getAllFlights(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_FLIGHT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        return ApiResponse.<PageResponse<FlightSearchResponseDTO>>builder()
                .message("Lấy danh sách chuyến bay thành công")
                .result(flightService.getAllFlights(page, size, sortBy, sortDir))
                .build();
    }
}
