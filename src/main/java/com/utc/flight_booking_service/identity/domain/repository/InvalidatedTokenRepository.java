package com.utc.flight_booking_service.identity.domain.repository;

import com.utc.flight_booking_service.identity.domain.entities.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}