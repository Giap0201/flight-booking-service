package com.utc.flight_booking_service.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopRouteResponse {
    private String route;
    private Long ticketCount;
    private Double percentage;
}