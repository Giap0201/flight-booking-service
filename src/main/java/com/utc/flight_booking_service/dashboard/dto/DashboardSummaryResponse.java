package com.utc.flight_booking_service.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalRevenue;
    private long totalBookings;
    private long totalTicketsIssued;
    private long totalCancelledBookings;
}
