package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Flight;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlightRepository extends JpaRepository<Flight, UUID>, JpaSpecificationExecutor<Flight> {
    Optional<Flight> findByAviationFlightId(String aviationFlightId);

    @EntityGraph(attributePaths = {"flightClasses", "airline"})
    List<Flight> findAll(Specification<Flight> spec);

    // lấy thông tin để trả cho mail
    @Query("SELECT DISTINCT f FROM Flight f " +
            "JOIN FETCH f.airline " +
            "JOIN FETCH f.origin " +
            "JOIN FETCH f.destination " +
            "JOIN FETCH f.flightClasses " +
            "WHERE f.id IN :flightIds")
    List<Flight> findAllFlightMasterData(@Param("flightIds") List<UUID> flightIds);
}
