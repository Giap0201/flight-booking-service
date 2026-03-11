package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.AppConstants;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.request.FlightSearchRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.CheapestDateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightDetailResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightSearchService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightController {
    IFlightSearchService flightSearchService;

    @PostMapping("/search")
    public ApiResponse<PageResponse<FlightSearchResponseDTO>> searchFlights(
            @Valid @RequestBody FlightSearchRequestDTO request,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_FLIGHT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        return ApiResponse.<PageResponse<FlightSearchResponseDTO>>builder()
                .message("Tìm kiếm chuyến bay thành công")
                .result(flightSearchService.searchAvailableFlights(request, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<FlightDetailResponseDTO> getFlightDetail(@PathVariable UUID id) {
        return ApiResponse.<FlightDetailResponseDTO>builder()
                .message("Lấy thông tin chi tiết chuyến bay thành công")
                .result(flightSearchService.getFlightDetail(id))
                .build();
    }

    @GetMapping("/cheapest-dates")
    public ApiResponse<List<CheapestDateResponseDTO>> getCheapestDates(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.<List<CheapestDateResponseDTO>>builder()
                .message("Lấy lịch giá rẻ thành công")
                .result(flightSearchService.getCheapestPricesInMonth(origin, destination, year, month))
                .build();
    }
}
