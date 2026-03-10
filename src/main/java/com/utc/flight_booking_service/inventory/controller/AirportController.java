package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;
import com.utc.flight_booking_service.inventory.service.IAirportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/airports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirportController {
    IAirportService airportService;

    @GetMapping
    public ApiResponse<List<AirportResponseDTO>> getAirports(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.<List<AirportResponseDTO>>builder()
                .message("Lấy danh sách sân bay thành công")
                .result(airportService.getAllAirports(keyword))
                .build();
    }
}