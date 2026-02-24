package com.utc.flight_booking_service.booking.impl;

import com.utc.flight_booking_service.booking.BookingRepository;
import com.utc.flight_booking_service.booking.BookingService;
import com.utc.flight_booking_service.booking.PnrGenerator;
import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.entity.BookingFlight;
import com.utc.flight_booking_service.booking.entity.Passenger;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.mapper.BookingFlightMapper;
import com.utc.flight_booking_service.booking.mapper.BookingMapper;
import com.utc.flight_booking_service.booking.mapper.PassengerMapper;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        // check ghe trong
//        flightService.reserveSeats(request.getFlightId(), request.getPassengers().size());

        Booking booking = bookingMapper.toBooking(request);
        String pnrCode = handlePnrCode();
        booking.setStatus(BookingStatus.PENDING);
        booking.setPnrCode(pnrCode);
        booking.setTotalAmount(new BigDecimal(10000));
        booking.setExpireAt(LocalDateTime.now().plusMinutes(10));

        request.getPassengers().forEach(passenger -> {
            Passenger p = passengerMapper.toPassenger(passenger);
            booking.addPassenger(p);
        });

        for (int i = 0; i < request.getFlights().size(); i++) {
            BookingFlight bookingFlight = bookingFlightMapper.toBookingFlight(request.getFlights().get(i));
            bookingFlight.setSegmentNo(i + 1);
            booking.addBookingFlight(bookingFlight);
        }

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(savedBooking);
    }

    private String handlePnrCode() {
        String pnrCode;
        int counter = 0;
        do {
            pnrCode = PnrGenerator.generatePnr();
            counter++;
            if (!bookingRepository.existsByPnrCode(pnrCode)) return pnrCode;
            if (counter > 5) throw new AppException(ErrorCode.CANNOT_CREATE_PNR_CODE);
        } while (true);
    }
}
