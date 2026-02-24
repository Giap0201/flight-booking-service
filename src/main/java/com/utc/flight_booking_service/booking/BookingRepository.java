package com.utc.flight_booking_service.booking;

import com.utc.flight_booking_service.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsByPnrCode(String pnrCode);
}
