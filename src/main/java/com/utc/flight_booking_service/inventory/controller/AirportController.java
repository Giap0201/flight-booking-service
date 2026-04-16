package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.AppConstants;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.request.AirportRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;
import com.utc.flight_booking_service.inventory.service.IAirportService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/airports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirportController {
    IAirportService airportService;

    @GetMapping
    public ApiResponse<PageResponse<AirportResponseDTO>> getAirports(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_AIRPORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        return ApiResponse.<PageResponse<AirportResponseDTO>>builder()
                .message("Lấy danh sách sân bay thành công")
                .result(airportService.getAllAirports(keyword, page, size, sortBy, sortDir))
                .build();
    }


    @PostMapping
    public ApiResponse<AirportResponseDTO> create(@RequestBody @Valid AirportRequestDTO request) {
        return ApiResponse.<AirportResponseDTO>builder()
                .result(airportService.createAirport(request))
                .build();
    }


    @PutMapping("/{code}")
    public ApiResponse<AirportResponseDTO> update(
            @PathVariable String code,
            @RequestBody @Valid AirportRequestDTO request
    ) {
        return ApiResponse.<AirportResponseDTO>builder()
                .result(airportService.updateAirport(code, request))
                .build();
    }


    @DeleteMapping("/{code}")
    public ApiResponse<Void> delete(@PathVariable String code) {
        airportService.deleteAirport(code);
        return ApiResponse.<Void>builder()
                .message("Xóa sân bay thành công")
                .build();
    }
}