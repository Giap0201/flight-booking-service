package com.utc.flight_booking_service.dashboard.service;

import com.utc.flight_booking_service.dashboard.dto.DailyRevenueResponse;
import com.utc.flight_booking_service.dashboard.dto.DashboardSummaryResponse;
import com.utc.flight_booking_service.dashboard.dto.response.TopRouteResponse;

import java.time.LocalDate;
import java.util.List;

public interface IDashboardService {
    public DashboardSummaryResponse getSummary(LocalDate startDate, LocalDate endDate);

    List<DailyRevenueResponse> getRevenueChart(LocalDate startDate, LocalDate endDate);

    List<TopRouteResponse> getTop5Routes(LocalDate startDate, LocalDate endDate);
}
