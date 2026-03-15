package com.utc.flight_booking_service.booking.entity;

import com.utc.flight_booking_service.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
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
    @Column(name = "segment_no", nullable = false)
    int segmentNo;

    @Column(name = "origin_flight_number")
    String originFlightNumber;

    @Column(name = "origin_departure_time")
    LocalDateTime originDepartureTime;

    @Column(name = "origin_arrival_time")
    LocalDateTime originArrivalTime;

    @Column(name = "flight_id", nullable = false)
    UUID flightId;

    @Column(name = "flight_class_id", nullable = false)
    UUID flightClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;
}
