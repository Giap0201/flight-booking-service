package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.AppConstants;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.response.AirlineResponseDTO;
import com.utc.flight_booking_service.inventory.service.IAirlineService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/airlines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirlineController {
    IAirlineService airlineService;

    @GetMapping
    public ApiResponse<PageResponse<AirlineResponseDTO>> getAirlines(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_AIRLINE_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        return ApiResponse.<PageResponse<AirlineResponseDTO>>builder()
                .message("Lấy danh sách hãng hàng không thành công")
                .result(airlineService.getAllAirlines(page, size, sortBy, sortDir))
                .build();
    }
}
