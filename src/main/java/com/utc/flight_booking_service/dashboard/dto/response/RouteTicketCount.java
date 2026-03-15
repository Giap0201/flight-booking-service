package com.utc.flight_booking_service.dashboard.dto.response;

import java.util.UUID;

public interface RouteTicketCount {
    UUID getFlightId();

    Long getTicketCount();
}
