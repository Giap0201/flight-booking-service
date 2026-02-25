package com.utc.flight_booking_service.identity.domain.repository;

import com.utc.flight_booking_service.identity.domain.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
