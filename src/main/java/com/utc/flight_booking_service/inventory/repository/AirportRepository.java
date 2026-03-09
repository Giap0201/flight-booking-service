package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
    @Query("SELECT a FROM Airport a WHERE " +
            "UPPER(a.code) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
            "UPPER(a.name) LIKE UPPER(CONCAT('%', :keyword, '%'))")
    List<Airport> searchAirports(@Param("keyword") String keyword);
}
