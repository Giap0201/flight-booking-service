package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.inventory.dto.response.AirlineResponseDTO;
import com.utc.flight_booking_service.inventory.service.IAirlineService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/airlines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirlineController {
    IAirlineService airlineService;

    @GetMapping
    public ApiResponse<List<AirlineResponseDTO>> getAirlines() {
        return ApiResponse.<List<AirlineResponseDTO>>builder()
                .message("Lấy danh sách hãng hàng không thành công")
                .result(airlineService.getAllAirlines())
                .build();
    }
}
