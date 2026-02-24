package com.utc.flight_booking_service.booking.impl;

import com.utc.flight_booking_service.booking.*;
import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.mapper.BookingFlightMapper;
import com.utc.flight_booking_service.booking.mapper.BookingMapper;
import com.utc.flight_booking_service.booking.mapper.PassengerMapper;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;
    BookingMapper bookingMapper;
    PassengerMapper passengerMapper;
    BookingFlightMapper bookingFlightMapper;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        Booking booking = bookingMapper.toBooking(request);
        booking.setPassengers(request.getPassengers().stream()
                .map(passengerMapper::toPassenger).toList());

        booking.setBookingFlights(request.getFlights().stream()
                .map(bookingFlightMapper::toBookingFlight).toList());
        bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(booking);
    }
}
