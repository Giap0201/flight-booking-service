package com.utc.flight_booking_service.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "flight_classes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"flight_id", "class_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightClass extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    Flight flight;

    @Enumerated(EnumType.STRING) // Quan trọng: Lưu dạng Text xuống DB thay vì số (0, 1, 2)
    @Column(name = "class_type", length = 20)
    FlightClassType classType;

    BigDecimal basePrice;
    Double taxPercentage;

    Integer totalSeats;
    Integer availableSeats;

    @Version
    Integer version;
}