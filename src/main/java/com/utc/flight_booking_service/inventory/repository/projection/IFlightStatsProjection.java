package com.utc.flight_booking_service.inventory.repository.projection;

public interface IFlightStatsProjection {
    Long getTotalFlights();
    Long getTotalSeats();
    Long getAvailableSeats();
}
