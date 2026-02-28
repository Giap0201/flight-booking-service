package com.utc.flight_booking_service.inventory.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.inventory.dto.request.FlightSearchRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightSearchService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightController {
    IFlightSearchService flightSearchService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<FlightSearchResponseDTO>>> searchFlights(@Valid @RequestBody FlightSearchRequestDTO request) {
        List<FlightSearchResponseDTO> flights = flightSearchService.searchAvailableFlights(request);

        ApiResponse<List<FlightSearchResponseDTO>> response = ApiResponse.<List<FlightSearchResponseDTO>>builder()
                .code(1000)
                .message("Tìm kiếm chuyến bay thành công")
                .result(flights)
                .build();

        return ResponseEntity.ok(response);
    }
}
