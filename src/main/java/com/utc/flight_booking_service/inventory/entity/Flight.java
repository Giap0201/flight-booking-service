package com.utc.flight_booking_service.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity //Lịch bay
@Table(name = "flights", indexes = {
        @Index(name = "idx_flight_search", columnList = "origin_code, destination_code, departure_time"),
        @Index(name = "idx_flight_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, length = 100)
    String aviationFlightId;                //ID gốc từ Aviationstack

    String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_code")
    Airline airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_code")
    Aircraft aircraft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_code")
    Airport origin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_code")
    Airport destination;

    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    String status; //SCHEDULED, DELAYED, CANCELLED

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FlightClass> flightClasses;
}
