package com.utc.flight_booking_service.booking.entity;

import com.utc.flight_booking_service.booking.enums.FlightDirection;
import com.utc.flight_booking_service.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "booking_flights")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingFlight extends BaseEntity {
    @Column(name = "price_at_booking")
    BigDecimal priceAtBooking;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    FlightDirection direction;

    @Column(name = "flight_id", nullable = false)
    UUID flightId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;
}
