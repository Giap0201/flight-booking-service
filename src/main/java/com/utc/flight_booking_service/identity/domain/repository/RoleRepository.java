package com.utc.flight_booking_service.identity.domain.repository;

import com.utc.flight_booking_service.identity.domain.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);

    List<Role> findAllByNameIn(Set<String> names);

    boolean existsByName(String name);
}
