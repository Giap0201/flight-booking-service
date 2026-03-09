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
import com.utc.flight_booking_service.booking.request.AdminBookingSearchRequest;
import com.utc.flight_booking_service.booking.request.BookingFlightRequest;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.request.BookingSearchRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingDetailResponse;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.client.*;
import com.utc.flight_booking_service.booking.response.share.ContactResponse;
import com.utc.flight_booking_service.booking.response.share.ETicketEmailModel;
import com.utc.flight_booking_service.booking.response.share.PageResponse;
import com.utc.flight_booking_service.booking.specification.BookingSpecification;
import com.utc.flight_booking_service.booking.utils.GeneratorCode;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import com.utc.flight_booking_service.identity.service.IUserService;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
import com.utc.flight_booking_service.payment.dto.response.AdminTransactionResponse;
import com.utc.flight_booking_service.payment.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    FlightRepository flightRepository;
    BookingMapper bookingMapper;
    PassengerMapper passengerMapper;
    BookingFlightMapper bookingFlightMapper;
    PriceService priceService;
    IFlightClassService flightClassService;
    IUserService userService;
    TransactionService transactionService;


    @Override
    public BookingCreatedResponse createBooking(BookingRequest request) {
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
        return bookingMapper.toBookingCreatedResponse(savedBooking);
    }

    @Override
    public BookingDetailResponse getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(()
                -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return mapToBookingDetailResponse(booking);
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
        return bookingRepository.findByPnrCode(pnrCode).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    @Override
    public Booking getBookingEntityById(UUID id) {
        return bookingRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));
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
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        log.info("Đã xuất vé thành công cho Booking: {}", booking.getPnrCode());
    }

    @Override
    public List<ETicketEmailModel> getTicketsByBookingId(UUID bookingId) {
        Booking booking = getBookingEntityById(bookingId);

        // Cache API BE1
        Map<UUID, FlightPriceResponseDTO> flightCache = new HashMap<>();
        Map<UUID, BookingFlight> snapshotCache = booking.getBookingFlights().stream()
                .collect(Collectors.toMap(BookingFlight::getFlightClassId, bf -> bf));

        List<ETicketEmailModel> responses = new ArrayList<>();

        for (Ticket ticket : booking.getTickets()) {
            UUID classId = ticket.getFlightClassId();
            FlightPriceResponseDTO flightInfo = flightCache.computeIfAbsent(classId, flightClassService::getFlightPrice);

            BookingFlight snapshotFlight = snapshotCache.get(classId);

            responses.add(ETicketEmailModel.builder()
                    .ticketNumber(ticket.getTicketNumber())
                    .pnrCode(booking.getPnrCode())
                    .status(ticket.getStatus())
                    .passengerFullName(ticket.getPassenger().getFirstName() + " " + ticket.getPassenger().getLastName())
                    .passengerType(ticket.getPassenger().getType())
                    .origin(flightInfo.getOrigin())
                    .destination(flightInfo.getDestination())
                    .totalAmount(ticket.getTotalAmount())
                    .classType(flightInfo.getClassType())
                    .seatNumber(ticket.getSeatNumber()) // Bổ sung số ghế
                    .flightNumber(snapshotFlight != null ? snapshotFlight.getOriginFlightNumber() : flightInfo.getFlightNumber())
                    .departureTime(snapshotFlight != null ? snapshotFlight.getOriginDepartureTime() : flightInfo.getDepartureTime())
                    .arrivalTime(snapshotFlight != null ? snapshotFlight.getOriginArrivalTime() : flightInfo.getArrivalTime())
                    .build());
        }

        return responses;
    }

    @Override
    public BookingDetailResponse getBookingClientByPnrAndContactEmail(BookingSearchRequest request) {
        Booking booking = bookingRepository.findByPnrCodeAndContactEmail(request.getPnrCode(), request.getContactEmail()).orElseThrow(() ->
                new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return mapToBookingDetailResponse(booking);
    }

    @Override
    public PageResponse<BookingSummaryResponse> getMyBookings(String filter, int page, int size) {
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

        List<BookingSummaryResponse> content = bookingPage.getContent().stream()
                .map(this::mapToBookingSummaryResponse).toList();
        return PageResponse.<BookingSummaryResponse>builder()
                .currentPage(page)
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalElements(bookingPage.getTotalElements())
                .content(content)
                .build();
    }

    @Override
    public void cancelUnpaidBooking(UUID bookingId) {
        UserResponse user = userService.getMyInfo();
        if (user == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        Booking booking = getBookingEntityById(bookingId);
        if (booking.getUserId() == null || !booking.getUserId().equals(user.getId())) {
            log.warn("User {} cố tình hủy Booking {} của người khác!", user.getId(), bookingId);
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            log.error("Không thể hủy vé vì trạng thái hiện tại là: {}", booking.getStatus());
            throw new AppException(ErrorCode.CANNOT_CANCEL_BOOKING);
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.getTickets().forEach(ticket -> ticket.setStatus((TicketStatus.CANCELLED)));
        int totalPassengers = booking.getPassengers().size();
        for (BookingFlight bookingFlight : booking.getBookingFlights()) {
            try {
                flightClassService.increaseSeats(bookingFlight.getFlightClassId(), totalPassengers);
                log.info("Đã trả lại {} ghế cho chuyến bay {}", totalPassengers, bookingFlight.getFlightClassId());
            } catch (AppException e) {
                log.error("Lỗi khi trả ghế cho chuyến bay {}: {}", bookingFlight.getFlightClassId(), e.getMessage());
            }
        }
        bookingRepository.save(booking);
        log.info("User {} đã tự hủy thành công Booking {}", user.getId(), booking.getPnrCode());
    }

    @Override
    public PageResponse<AdminBookingSummaryResponse> searchBookingsForAdmin(AdminBookingSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Specification<Booking> spec = BookingSpecification.getSearchSpec(request);
        Page<Booking> bookingPage = bookingRepository.findAll(spec, pageable);

        List<AdminBookingSummaryResponse> content = bookingPage.getContent().stream()
                .map(this::mapToAdminBookingSummaryResponse)
                .toList();
        return PageResponse.<AdminBookingSummaryResponse>builder()
                .currentPage(page)
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalElements(bookingPage.getTotalElements())
                .content(content)
                .build();
    }

    @Override
    public AdminBookingDetailResponse getBookingDetailsForAdmin(UUID id) {
        Booking booking = getBookingEntityById(id);
        BookingDetailResponse bookingDetailResponse = mapToBookingDetailResponse(booking);
        List<AdminTransactionResponse> transactionResponse = transactionService.getTransactionsByBookingId(id);
        return AdminBookingDetailResponse.builder()
                .baseDetails(bookingDetailResponse)
                .transactions(transactionResponse)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .userId(booking.getUserId())
                .build();
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

    private BookingSummaryResponse mapToBookingSummaryResponse(Booking booking) {
        BookingSummaryResponse summary = BookingSummaryResponse.builder()
                .id(booking.getId())
                .pnrCode(booking.getPnrCode())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .build();

        if (booking.getBookingFlights() != null && !booking.getBookingFlights().isEmpty()) {
            BookingFlight firstBookingFlight = booking.getBookingFlights().stream()
                    .filter(f -> f.getSegmentNo() == 1)
                    .findFirst()
                    .orElse(booking.getBookingFlights().get(0));

            FlightPriceResponseDTO flightInfo = flightClassService.getFlightPrice(firstBookingFlight.getFlightClassId());
            summary.setFlightNumber(firstBookingFlight.getOriginFlightNumber());
            summary.setOrigin(flightInfo.getOrigin());
            summary.setDestination(flightInfo.getDestination());
            summary.setDepartureTime(firstBookingFlight.getOriginDepartureTime());
        }
        return summary;
    }

    private BookingDetailResponse mapToBookingDetailResponse(Booking booking) {
        ContactResponse contact = ContactResponse.builder()
                .name(booking.getContactName())
                .email(booking.getContactEmail())
                .phone(booking.getContactPhone())
                .build();

        Map<UUID, FlightPriceResponseDTO> flightCache = new HashMap<>();

        List<PassengerTicketResponse> passengerResponses = booking.getPassengers().stream().map(passenger -> {
            List<TicketDetailResponse> ticketResponses = booking.getTickets().stream()
                    .filter(ticket -> ticket.getPassenger().getId().equals(passenger.getId()))
                    .map(ticket -> {
                        BookingFlight snapshotFlight = booking.getBookingFlights().stream()
                                .filter(bf -> bf.getFlightClassId().equals(ticket.getFlightClassId()))
                                .findFirst()
                                .orElse(null);
                        FlightPriceResponseDTO be1Info = flightCache.computeIfAbsent(
                                ticket.getFlightClassId(), flightClassService::getFlightPrice);
                        return TicketDetailResponse.builder()
                                .ticketNumber(ticket.getTicketNumber())
                                .status(ticket.getStatus())
                                .seatNumber(ticket.getSeatNumber())
                                .totalAmount(ticket.getTotalAmount())
                                .flightNumber(snapshotFlight != null ? snapshotFlight.getOriginFlightNumber() : be1Info.getFlightNumber())
                                .departureTime(snapshotFlight != null ? snapshotFlight.getOriginDepartureTime() : be1Info.getDepartureTime())
                                .arrivalTime(snapshotFlight != null ? snapshotFlight.getOriginArrivalTime() : be1Info.getArrivalTime())
                                .departureAirport(be1Info.getOrigin())
                                .arrivalAirport(be1Info.getDestination())
                                .classType(be1Info.getClassType())
                                .build();
                    }).toList();

            return PassengerTicketResponse.builder()
                    .passengerId(passenger.getId())
                    .firstName(passenger.getFirstName())
                    .lastName(passenger.getLastName())
                    .type(passenger.getType())
                    .tickets(ticketResponses)
                    .build();
        }).toList();

        return BookingDetailResponse.builder()
                .id(booking.getId())
                .pnrCode(booking.getPnrCode())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .currency(booking.getCurrency() != null ? booking.getCurrency() : "VND")
                .contact(contact)
                .passengers(passengerResponses)
                .build();
    }

    private AdminBookingSummaryResponse mapToAdminBookingSummaryResponse(Booking booking) {
        AdminBookingSummaryResponse summary = AdminBookingSummaryResponse.builder()
                .id(booking.getId())
                .pnrCode(booking.getPnrCode())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .contactName(booking.getContactName())
                .contactPhone(booking.getContactPhone())
                .contactEmail(booking.getContactEmail())
                .build();

        // Tái sử dụng logic lấy chuyến bay đầu tiên
        if (booking.getBookingFlights() != null && !booking.getBookingFlights().isEmpty()) {
            BookingFlight firstBookingFlight = booking.getBookingFlights().stream()
                    .filter(f -> f.getSegmentNo() == 1)
                    .findFirst()
                    .orElse(booking.getBookingFlights().get(0));

            FlightPriceResponseDTO flightInfo = flightClassService.getFlightPrice(firstBookingFlight.getFlightClassId());
            summary.setFlightNumber(firstBookingFlight.getOriginFlightNumber());
            summary.setOrigin(flightInfo.getOrigin());
            summary.setDestination(flightInfo.getDestination());
            summary.setDepartureTime(firstBookingFlight.getOriginDepartureTime());
        }
        return summary;
    }
}
