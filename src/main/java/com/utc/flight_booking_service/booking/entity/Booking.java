package com.utc.flight_booking_service.booking.entity;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "bookings")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Booking extends BaseEntity {
    @Column(name = "pnr_code", nullable = false, unique = true, length = 10)
    String pnrCode;

    @Column(name = "contact_email")
    String contactEmail;

    @Column(name = "contact_phone")
    String contactPhone;

    @Column(name = "contact_name")
    String contactName;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

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

    public void addPassenger(Passenger passenger) {
        if(this.passengers == null) {
            this.passengers = new ArrayList<>();
        }
        passengers.add(passenger);
        passenger.setBooking(this);
    }

    public void addBookingFlight(BookingFlight bookingFlight) {
        if(this.bookingFlights == null) {
            this.bookingFlights = new ArrayList<>();
        }
        bookingFlights.add(bookingFlight);
        bookingFlight.setBooking(this);
    }

}
