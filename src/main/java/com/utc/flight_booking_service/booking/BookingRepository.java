package com.utc.flight_booking_service.booking;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    boolean existsByPnrCode(String pnrCode);

    List<Booking> findByStatusAndExpireAtBefore(BookingStatus bookingStatus, LocalDateTime now);

    Optional<Booking> findByPnrCode(String pnrCode);
}
