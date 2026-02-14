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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightClass extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    Flight flight;

    @Column(length = 20)
    String classType; //ECONOMY, BUSINESS

    BigDecimal basePrice;
    Double taxPercentage;

    Integer totalSeats;
    Integer availableSeats;

    @Version
    Integer version;
}
