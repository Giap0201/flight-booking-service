package com.utc.flight_booking_service.booking.entity;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_expire_at", columnList = "expire_at"),
        @Index(name = "idx_booking_pnr", columnList = "pnr_code")
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Booking extends BaseEntity {
    @Column(name = "pnr_code", nullable = false, unique = true, length = 10)
    String pnrCode;

    @Column(name = "contact_email", nullable = false)
    String contactEmail;

    @Column(name = "contact_phone", nullable = false)
    String contactPhone;

    @Column(name = "contact_name", nullable = false)
    String contactName;

    @Column(name = "total_fare_amount")
    BigDecimal totalFareAmount;

    @Column(name = "total_tax_amount")
    BigDecimal totalTaxAmount;

    @Column(name = "total_discount_amount")
    @Builder.Default
    BigDecimal totalDiscountAmount = BigDecimal.ZERO;

    @Column(name = "promotion_code")
    String promotionCode;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    BookingStatus status;

    @Column(name = "currency", length = 3)
    @Builder.Default
    String currency = "VND";

    @Column(name = "expire_at")
    LocalDateTime expireAt;

    @Column(name = "user_id")
    UUID userId;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "booking",
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    List<Passenger> passengers = new ArrayList<>();

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "booking",
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    List<BookingFlight> bookingFlights = new ArrayList<>();

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "booking",
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    List<Ticket> tickets = new ArrayList<>();

    public void addPassenger(Passenger passenger) {
        if (this.passengers == null) {
            this.passengers = new ArrayList<>();
        }
        passengers.add(passenger);
        passenger.setBooking(this);
    }

    public void addBookingFlight(BookingFlight bookingFlight) {
        if (this.bookingFlights == null) {
            this.bookingFlights = new ArrayList<>();
        }
        bookingFlights.add(bookingFlight);
        bookingFlight.setBooking(this);
    }

    public void addTicket(Ticket ticket) {
        if (this.tickets == null) {
            this.tickets = new ArrayList<>();
        }
        tickets.add(ticket);
        ticket.setBooking(this);
    }

}
