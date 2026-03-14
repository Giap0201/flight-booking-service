package com.utc.flight_booking_service.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyRevenueResponse {
    LocalDate getReportDate();

    BigDecimal getRevenue();

    Integer getBookingCount();
}
