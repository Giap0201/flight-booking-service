package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByAviationFlightId(String aviationFlightId);
}
