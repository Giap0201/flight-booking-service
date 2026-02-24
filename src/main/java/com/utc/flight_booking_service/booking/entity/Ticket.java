package com.utc.flight_booking_service.booking.entity;

import com.utc.flight_booking_service.booking.enums.TicketStatus;
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
@Table(name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"passenger_id", "flight_id"})},
        indexes = {
                @Index(name = "idx_ticket_number", columnList = "ticket_number"),
                @Index(name = "idx_ticket_booking_id", columnList = "booking_id")
        })
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ticket extends BaseEntity {
    @Column(name = "ticket_number", unique = true)
    String ticketNumber;

    @Column(name = "base_fare", nullable = false, precision = 15, scale = 2)
    BigDecimal baseFare;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    BigDecimal taxAmount;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    BigDecimal totalAmount;

    @Column(name = "seat_number")
    String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    TicketStatus status = TicketStatus.RESERVED;

//    @Column(name = "passenger_id", nullable = false)
//    UUID passengerId;

    @Column(name = "flight_id", nullable = false)
    UUID flightId;

    @Column(name = "flight_class_id", nullable = false)
    UUID flightClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    Passenger passenger;
}
