package com.utc.flight_booking_service.booking.entity;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
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
    List<Passenger> passengers;

    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "booking"
    )
    List<BookingFlight> bookingFlights;

}
