package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, String> {
    
}
