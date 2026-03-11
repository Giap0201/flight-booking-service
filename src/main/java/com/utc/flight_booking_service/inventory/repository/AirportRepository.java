package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Airport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
    @Query("SELECT a FROM Airport a WHERE " +
            "UPPER(a.code) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
            "UPPER(a.name) LIKE UPPER(CONCAT('%', :keyword, '%'))")
    Page<Airport> searchAirports(@Param("keyword") String keyword, Pageable pageable);
}
