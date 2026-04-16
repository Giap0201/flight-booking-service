package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.repository.projection.ICheapestPriceProjection;
import com.utc.flight_booking_service.inventory.repository.projection.IFlightStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlightRepository extends JpaRepository<Flight, UUID>, JpaSpecificationExecutor<Flight> {
    Optional<Flight> findByAviationFlightId(String aviationFlightId);

    @Override
    @EntityGraph(attributePaths = {"airline", "origin", "destination", "aircraft"})
    List<Flight> findAll(Specification<Flight> spec);

    @Override
    @EntityGraph(attributePaths = {"airline", "origin", "destination", "aircraft"})
    Page<Flight> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"airline", "origin", "destination", "aircraft"})
    Page<Flight> findAll(Specification<Flight> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"airline", "origin", "destination", "aircraft"})
    List<Flight> findAllByIdIn(List<UUID> ids);

    @Query("SELECT COUNT(DISTINCT f.id) as totalFlights, " +
            "SUM(fc.totalSeats) as totalSeats, " +
            "SUM(fc.availableSeats) as availableSeats " +
            "FROM Flight f JOIN f.flightClasses fc " +
            "WHERE f.departureTime BETWEEN :start AND :end " +
            "AND f.status = 'SCHEDULED'")
    IFlightStatsProjection getRawStatistics(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT CAST(f.departureTime AS LocalDate) as departureDate, MIN(fc.basePrice) as minPrice " +
            "FROM Flight f JOIN f.flightClasses fc " +
            "WHERE f.origin.code = :origin " +
            "AND f.destination.code = :destination " +
            "AND f.departureTime BETWEEN :start AND :end " +
            "AND f.status = 'SCHEDULED' " +
            "GROUP BY CAST(f.departureTime AS LocalDate) " +
            "ORDER BY CAST(f.departureTime AS LocalDate) ASC")
    List<ICheapestPriceProjection> findCheapestPricesByMonth(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
