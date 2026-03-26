package com.utc.flight_booking_service.dashboard.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.dashboard.dto.DailyRevenueResponse;
import com.utc.flight_booking_service.dashboard.dto.DashboardSummaryResponse;
import com.utc.flight_booking_service.dashboard.dto.response.TopRouteResponse;
import com.utc.flight_booking_service.dashboard.service.IDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDashboardController {

    IDashboardService dashboardService;


    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ApiResponse.<DashboardSummaryResponse>builder()
                .result(dashboardService.getSummary(startDate, endDate))
                .build();
    }


    @GetMapping("/charts/revenue")
    public ApiResponse<List<DailyRevenueResponse>> getRevenueChart(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ApiResponse.<List<DailyRevenueResponse>>builder()
                .result(dashboardService.getRevenueChart(startDate, endDate))
                .build();
    }

    @GetMapping("/charts/top-routes")
    public ApiResponse<List<TopRouteResponse>> getTopRoutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ApiResponse.<List<TopRouteResponse>>builder()
                .result(dashboardService.getTop5Routes(startDate, endDate))
                .build();
    }
}