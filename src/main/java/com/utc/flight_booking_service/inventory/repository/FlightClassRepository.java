package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.FlightClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlightClassRepository extends JpaRepository<FlightClass, UUID> {
}
