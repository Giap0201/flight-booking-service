package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Flight;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String>, JpaSpecificationExecutor<Flight> {
    Optional<Flight> findByAviationFlightId(String aviationFlightId);

    @EntityGraph(attributePaths = {"flightClasses", "airline"})
    List<Flight> findAll(Specification<Flight> spec);
}
