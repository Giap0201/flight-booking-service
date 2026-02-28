package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.entity.Aircraft;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightClass;

import java.util.List;

public interface IFlightEnrichmentService {
    List<FlightClass> enrichFlight(Flight flight, Aircraft aircraft);
}
