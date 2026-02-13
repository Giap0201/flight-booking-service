package com.utc.flight_booking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FlightBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightBookingServiceApplication.class, args);
    }

}
