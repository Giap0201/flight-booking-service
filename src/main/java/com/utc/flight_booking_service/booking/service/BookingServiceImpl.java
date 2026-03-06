package com.utc.flight_booking_service.booking.service;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.entity.BookingFlight;
import com.utc.flight_booking_service.booking.entity.Passenger;
import com.utc.flight_booking_service.booking.entity.Ticket;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.booking.mapper.BookingFlightMapper;
import com.utc.flight_booking_service.booking.mapper.BookingMapper;
import com.utc.flight_booking_service.booking.mapper.PassengerMapper;
import com.utc.flight_booking_service.booking.repository.BookingRepository;
import com.utc.flight_booking_service.booking.repository.TicketRepository;
import com.utc.flight_booking_service.booking.request.BookingFlightRequest;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.request.BookingSearchRequest;
import com.utc.flight_booking_service.booking.response.BookingDetailsResponse;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import com.utc.flight_booking_service.booking.response.BookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.ClientETicketResponse;
import com.utc.flight_booking_service.booking.utils.GeneratorCode;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import com.utc.flight_booking_service.identity.service.IUserService;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    BookingMapper bookingMapper;
    PassengerMapper passengerMapper;
    BookingFlightMapper bookingFlightMapper;
    PriceService priceService;
    IFlightClassService flightClassService;
    IUserService userService;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        UserResponse user = userService.getMyInfo();
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
        booking.setUserId(user.getId());
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
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
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
    public int deleteByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt) {
        return bookingRepository.deleteByStatusAndCreatedAtBefore(status, createdAt);
    }

    @Override
    public Booking getBookingEntityByPnr(String pnrCode) {
        return bookingRepository.findByPnrCode(pnrCode).orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    @Override
    public Booking getBookingEntityById(UUID id) {
        return bookingRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    @Override
    public void updateBookingStatus(UUID id, BookingStatus status) {
        Booking booking = getBookingEntityById(id);
        booking.setStatus(status);
    }

    @Override
    public void issueTicketsForBooking(UUID bookingId) {
        Booking booking = getBookingEntityById(bookingId);
        if (booking.getTickets().stream().anyMatch(t -> t.getStatus() == TicketStatus.ISSUED)) {
            log.warn("Booking {} đã được xuất vé từ trước!", bookingId);
            return;
        }
        for (Ticket ticket : booking.getTickets()) {
            if (ticket.getStatus() == TicketStatus.RESERVED) {
                Long sequence = ticketRepository.getNextTicketSequence();
                String ticketNumber = "738" + String.format("%010d", sequence);
                ticket.setTicketNumber(ticketNumber);
                ticket.setStatus(TicketStatus.ISSUED);
            }
        }
        bookingRepository.save(booking);
        log.info("Đã xuất vé thành công cho Booking: {}", booking.getPnrCode());
    }

    @Override
    public List<ClientETicketResponse> getTicketsByBookingId(UUID bookingId) {
        Booking booking = getBookingEntityById(bookingId);
        Map<UUID, FlightPriceResponseDTO> flightCache = new HashMap<>();
        List<ClientETicketResponse> responses = new ArrayList<>();
        for (Ticket ticket : booking.getTickets()) {
            UUID classId = ticket.getFlightClassId();
            // Nếu trong Map chưa có -> Gọi BE1. Nếu có rồi -> Lấy luôn từ Map
            FlightPriceResponseDTO flightInfo = flightCache.computeIfAbsent(classId, flightClassService::getFlightPrice);

            responses.add(ClientETicketResponse.builder()
                    .ticketNumber(ticket.getTicketNumber())
                    .pnrCode(booking.getPnrCode())
                    .status(ticket.getStatus())
                    .passengerFullName(ticket.getPassenger().getFirstName() + " " + ticket.getPassenger().getLastName())
                    .passengerType(ticket.getPassenger().getType())
                    .flightNumber(flightInfo.getFlightNumber())
                    .origin(flightInfo.getOrigin())
                    .destination(flightInfo.getDestination())
                    .departureTime(flightInfo.getDepartureTime())
                    .arrivalTime(flightInfo.getArrivalTime())
                    .totalAmount(ticket.getTotalAmount())
                    .classType(flightInfo.getClassType())
                    .build());
        }

        return responses;
    }

    @Override
    public BookingDetailsResponse getBookingClientByPnrAndContactEmail(BookingSearchRequest request) {
        Booking booking = bookingRepository.findByPnrCodeAndContactEmail(request.getPnrCode(), request.getContactEmail()).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<ClientETicketResponse> eTickets = getTicketsByBookingId(booking.getId());
        return BookingDetailsResponse.builder()
                .pnrCode(booking.getPnrCode())
                .status(booking.getStatus())
                .grandTotal(booking.getTotalAmount())
                .expireAt(booking.getExpireAt())
                .tickets(eTickets)
                .build();
    }

    @Override
    public Page<BookingSummaryResponse> getMyBookings(String filter, int page, int size) {
        UserResponse user = userService.getMyInfo();

        //Spring Boot tính page từ số 0, nên nếu FE gửi page 1, ta phải trừ đi 1.
        Pageable pageable = PageRequest.of(page - 1, size);
        LocalDateTime now = LocalDateTime.now();
        Page<Booking> bookingPage;

        List<BookingStatus> successStatuses = List.of(BookingStatus.PAID, BookingStatus.CONFIRMED);
        List<BookingStatus> cancelStatuses = List.of(BookingStatus.CANCELLED, BookingStatus.REFUNDED);
        if ("UPCOMING".equalsIgnoreCase(filter)) {
            bookingPage = bookingRepository.findUpcomingBookings(user.getId(), successStatuses, now, pageable);
        } else if ("COMPLETED".equalsIgnoreCase(filter)) {
            bookingPage = bookingRepository.findCompletedBookings(user.getId(), successStatuses, now, pageable);
        } else if ("CANCELLED".equalsIgnoreCase(filter)) {
            bookingPage = bookingRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(user.getId(), cancelStatuses, pageable);
        } else {
            bookingPage = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        }

        return bookingPage.map(booking -> {
            BookingSummaryResponse summary = BookingSummaryResponse.builder()
                    .id(booking.getId())
                    .pnrCode(booking.getPnrCode())
                    .status(booking.getStatus())
                    .totalAmount(booking.getTotalAmount())
                    .createdAt(booking.getCreatedAt())
                    .build();
            // Lay chuyen bay dau tien de lap them thong tin
            if (booking.getBookingFlights() != null && !booking.getBookingFlights().isEmpty()) {
                BookingFlight firstBookingFlight = booking.getBookingFlights().stream()
                        .filter(f -> f.getSegmentNo() == 1)
                        .findFirst()
                        .orElse(booking.getBookingFlights().get(0));

                FlightPriceResponseDTO flightInfo = flightClassService.getFlightPrice(firstBookingFlight.getFlightClassId());
                summary.setFlightNumber(flightInfo.getFlightNumber());
                summary.setOrigin(flightInfo.getOrigin());
                summary.setDestination(flightInfo.getDestination());
                summary.setDepartureTime(firstBookingFlight.getOriginDepartureTime());
            }
            return summary;
        });
    }

    private String handlePnrCode() {
        String pnrCode;
        int counter = 0;
        do {
            pnrCode = GeneratorCode.generatePnr();
            counter++;
            if (!bookingRepository.existsByPnrCode(pnrCode)) return pnrCode;
            if (counter > 5) throw new AppException(ErrorCode.CANNOT_CREATE_PNR_CODE);
        } while (true);
    }
}
