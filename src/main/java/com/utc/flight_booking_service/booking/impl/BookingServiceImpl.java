package com.utc.flight_booking_service.booking.impl;

import com.utc.flight_booking_service.booking.repository.BookingRepository;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.booking.service.PriceService;
import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.entity.BookingFlight;
import com.utc.flight_booking_service.booking.entity.Passenger;
import com.utc.flight_booking_service.booking.entity.Ticket;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.booking.mapper.BookingFlightMapper;
import com.utc.flight_booking_service.booking.mapper.BookingMapper;
import com.utc.flight_booking_service.booking.mapper.PassengerMapper;
import com.utc.flight_booking_service.booking.request.BookingFlightRequest;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import com.utc.flight_booking_service.booking.utils.PnrGenerator;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    PriceService priceService;
    IFlightClassService flightClassService;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        int totalPassengers = request.getPassengers().size();

        // Kiem tra va giu ghe (be1)
        for (BookingFlightRequest bookingFlightRequest : request.getFlights()) {
            flightClassService.decreaseSeats(bookingFlightRequest.getFlightClassId(), totalPassengers);
        }
        Booking booking = bookingMapper.toBooking(request);
        String pnrCode = handlePnrCode();
        booking.setStatus(BookingStatus.PENDING);
        booking.setPnrCode(pnrCode);
        booking.setExpireAt(LocalDateTime.now().plusMinutes(10));

        request.getPassengers().forEach(passenger -> {
            Passenger p = passengerMapper.toPassenger(passenger);
            booking.addPassenger(p);
        });

        for (int i = 0; i < request.getFlights().size(); i++) {
            BookingFlightRequest fReq = request.getFlights().get(i);
            BookingFlight bookingFlight = bookingFlightMapper.toBookingFlight(request.getFlights().get(i));
            FlightPriceResponseDTO flightInfo = flightClassService.getFlightPrice(fReq.getFlightClassId());
            bookingFlight.setSegmentNo(i + 1);
            bookingFlight.setOriginFlightNumber(flightInfo.getFlightNumber());
            bookingFlight.setOriginDepartureTime(flightInfo.getDepartureTime());
            bookingFlight.setOriginArrivalTime(flightInfo.getArrivalTime());
            booking.addBookingFlight(bookingFlight);
        }

        List<Ticket> tickets = priceService.calculateTickets(booking, request.getFlights());

        BigDecimal totalBookingAmount = tickets.stream().map(Ticket::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFare = tickets.stream().map(Ticket::getBaseFare).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = tickets.stream().map(Ticket::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        booking.setTotalAmount(totalBookingAmount);
        booking.setTotalFareAmount(totalFare);
        booking.setTotalTaxAmount(totalTax);
        tickets.forEach(booking::addTicket);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpireAtBefore(BookingStatus.PENDING, now);
        if (expiredBookings.isEmpty()) {
            log.info("Không có booking quá hạn");
            return;
        }
        log.info("Tìm thấy {} booking quá hạn, bắt đầu huỷ", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getTickets().forEach(ticket -> {
                ticket.setStatus(TicketStatus.CANCELLED);
            });
            int totalPassengers = booking.getPassengers().size();
            for (BookingFlight bookingFlight : booking.getBookingFlights()) {
                try {
                    flightClassService.increaseSeats(bookingFlight.getFlightClassId(), totalPassengers);
                    log.info("Đã trả lại {} ghế cho chuyến bay {}", totalPassengers, bookingFlight.getFlightClassId());
                } catch (AppException e) {
                    log.error("Lỗi khi trả ghế cho chuyến bay {}: {}", bookingFlight.getFlightClassId(), e.getMessage());
                }
            }
        }
    }

    @Override
    public Booking getBookingEntityByPnr(String pnrCode) {
        return bookingRepository.findByPnrCode(pnrCode).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    @Override
    public Booking getBookingEntityById(UUID id) {
        return bookingRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));
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
