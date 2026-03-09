package com.utc.flight_booking_service.booking.entity;

import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogType;
import com.utc.flight_booking_service.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "ancillary_catalogs", indexes = {
        @Index(name = "idx_catalog_code", columnList = "code", unique = true)
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AncillaryCatalog extends BaseEntity {
    @Column(name = "code", nullable = false, unique = true, length = 50)
    String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    AncillaryCatalogType type;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    AncillaryCatalogStatus status = AncillaryCatalogStatus.ACTIVE;
}