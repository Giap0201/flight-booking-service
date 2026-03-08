package com.utc.flight_booking_service.booking.repository;

import com.utc.flight_booking_service.booking.entity.AncillaryCatalog;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AncillaryCatalogRepository extends JpaRepository<AncillaryCatalog, UUID>, JpaSpecificationExecutor<AncillaryCatalog> {
    boolean existsByCode(String code);

    boolean existsByName(String name);

    List<AncillaryCatalog> findByStatus(AncillaryCatalogStatus status);
}
