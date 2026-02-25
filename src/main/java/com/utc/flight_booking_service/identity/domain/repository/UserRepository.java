package com.utc.flight_booking_service.identity.domain.repository;

import com.utc.flight_booking_service.identity.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String Phone);

    Optional<User> findByEmail(String email);
}
