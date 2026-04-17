package com.utc.flight_booking_service.booking.service;

import com.utc.flight_booking_service.booking.entity.*;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.enums.PassengerType;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.booking.mapper.BookingDtoMapper;
import com.utc.flight_booking_service.booking.mapper.BookingFlightMapper;
import com.utc.flight_booking_service.booking.mapper.BookingMapper;
import com.utc.flight_booking_service.booking.mapper.PassengerMapper;
import com.utc.flight_booking_service.booking.repository.AncillaryCatalogRepository;
import com.utc.flight_booking_service.booking.repository.BookingRepository;
import com.utc.flight_booking_service.booking.repository.TicketRepository;
import com.utc.flight_booking_service.booking.request.*;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingDetailResponse;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.client.BookingCreatedResponse;
import com.utc.flight_booking_service.booking.response.client.BookingDetailResponse;
import com.utc.flight_booking_service.booking.response.client.BookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.share.ETicketEmailModel;
import com.utc.flight_booking_service.booking.specification.BookingSpecification;
import com.utc.flight_booking_service.booking.utils.PassengerUtils;
import com.utc.flight_booking_service.booking.utils.PnrGenerator;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import com.utc.flight_booking_service.identity.service.IUserService;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
import com.utc.flight_booking_service.notification.service.EmailService;
import com.utc.flight_booking_service.payment.entity.Transaction;
import com.utc.flight_booking_service.payment.service.TransactionService;
import com.utc.flight_booking_service.payment.service.VNPayRefundService;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {

    private static final int PAYMENT_TIMEOUT_MINUTES = 15;
    private static final String TICKET_PREFIX = "738";
    private static final int CANCEL_BUFFER_HOURS = 4; // Không cho phép hủy/xuất vé trước giờ bay 4 tiếng

    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    AncillaryCatalogRepository ancillaryCatalogRepository;
    BookingMapper bookingMapper;
    PassengerMapper passengerMapper;
    BookingFlightMapper bookingFlightMapper;
    PriceService priceService;
    IFlightClassService flightClassService;
    IUserService userService;
    TransactionService transactionService;
    VNPayRefundService vnPayRefundService;
    EmailService emailService;
    BookingDtoMapper bookingDtoMapper;
    PnrGenerator pnrGenerator;

    @Override
    @Transactional
    public BookingCreatedResponse createBooking(BookingRequest request) {
        UserResponse user = userService.getMyInfo();
        int totalPassengers = request.getPassengers().size();

        List<UUID> reservedFlightClasses = new ArrayList<>();

        try {
            for (BookingFlightRequest fReq : request.getFlights()) {
                flightClassService.decreaseSeats(fReq.getFlightClassId(), totalPassengers);
                reservedFlightClasses.add(fReq.getFlightClassId());
            }

            Booking booking = bookingMapper.toBooking(request);
            booking.setStatus(BookingStatus.PENDING);
            booking.setPnrCode(pnrGenerator.generate());
            booking.setExpireAt(LocalDateTime.now().plusMinutes(PAYMENT_TIMEOUT_MINUTES));
            booking.setUserId(user.getId());

            request.getPassengers().forEach(passenger ->
                    booking.addPassenger(passengerMapper.toPassenger(passenger)));

            for (int i = 0; i < request.getFlights().size(); i++) {
                BookingFlightRequest fReq = request.getFlights().get(i);
                BookingFlight bookingFlight = bookingFlightMapper.toBookingFlight(fReq);
                FlightPriceResponseDTO flightInfo = flightClassService.getFlightPrice(fReq.getFlightClassId());
                bookingFlight.setSegmentNo(i + 1);
                bookingFlight.setOriginFlightNumber(flightInfo.getFlightNumber());
                bookingFlight.setOriginDepartureTime(flightInfo.getDepartureTime());
                bookingFlight.setOriginArrivalTime(flightInfo.getArrivalTime());
                booking.addBookingFlight(bookingFlight);
            }

            List<Ticket> tickets = priceService.calculateTickets(booking, request.getFlights());
            BigDecimal totalFare = tickets.stream().map(Ticket::getBaseFare).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalTax = tickets.stream().map(Ticket::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalTicketAmount = tickets.stream().map(Ticket::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            tickets.forEach(booking::addTicket);

            BigDecimal totalAncillaryAmount = BigDecimal.ZERO;

            if (request.getBookingAncillaries() != null && !request.getBookingAncillaries().isEmpty()) {
                for (BookingAncillaryRequest ancReq : request.getBookingAncillaries()) {
                    AncillaryCatalog catalog = ancillaryCatalogRepository.findByIdAndStatus(ancReq.getCatalogId(), AncillaryCatalogStatus.ACTIVE)
                            .orElseThrow(() -> new AppException(ErrorCode.ANCILLARY_CATALOG_NOT_FOUND));
                    Passenger p = booking.getPassengers().get(ancReq.getPassengerIndex());
                    BookingFlight bf = booking.getBookingFlights().stream()
                            .filter(f -> f.getSegmentNo() == ancReq.getSegmentNo())
                            .findFirst()
                            .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

                    BookingAncillary ancillary = BookingAncillary.builder()
                            .booking(booking).catalog(catalog).passenger(p)
                            .bookingFlight(bf).amount(catalog.getPrice())
                            .build();
                    totalAncillaryAmount = totalAncillaryAmount.add(catalog.getPrice());
                    booking.addBookingAncillary(ancillary);
                }
            }

            booking.setTotalAmount(totalTicketAmount.add(totalAncillaryAmount));
            booking.setTotalFareAmount(totalFare);
            booking.setTotalTaxAmount(totalTax);

            Booking savedBooking = bookingRepository.save(booking);
            return bookingMapper.toBookingCreatedResponse(savedBooking);

        } catch (Exception e) {
            log.error("Lỗi khi tạo booking, tiến hành rollback ghế cho các chuyến đã giữ: {}", reservedFlightClasses);
            for (UUID flightClassId : reservedFlightClasses) {
                try {
                    flightClassService.increaseSeats(flightClassId, totalPassengers);
                } catch (Exception rollbackEx) {
                    log.error("Lỗi khi rollback ghế chuyến bay {}: {}", flightClassId, rollbackEx.getMessage());
                }
            }
            throw new AppException(ErrorCode.BOOKING_CREATION_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingById(UUID id) {
        Booking booking = getBookingEntityById(id);
        BookingDetailResponse response = bookingDtoMapper.mapToBookingDetailResponse(booking);
        response.setTransactions(transactionService.getClientTransactionsByBookingId(booking.getId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getExpiredBookingsByStatus(BookingStatus bookingStatus) {
        return bookingRepository.findByStatusAndExpireAtBefore(bookingStatus, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void cancelSingleBookingBySystem(UUID bookingId) {
        Booking booking = getBookingEntityById(bookingId);
        log.info("Hệ thống tiến hành hủy PNR: {} và nhả ghế...", booking.getPnrCode());
        processCancellationLogic(booking);
    }

    @Override
    @Transactional
    public int deleteByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt) {
        return bookingRepository.deleteByStatusAndCreatedAtBefore(status, createdAt);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingEntityByPnr(String pnrCode) {
        return bookingRepository.findByPnrCode(pnrCode).orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingEntityById(UUID id) {
        return bookingRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    @Override
    @Transactional
    public void updateBookingStatus(UUID id, BookingStatus status) {
        Booking booking = getBookingEntityById(id);
        booking.setStatus(status);
    }

    @Override
    @Transactional
    public void issueTicketsForBooking(UUID bookingId) {
        Booking booking = getBookingEntityById(bookingId);
        validateDepartureTime(booking);

        if (booking.getTickets().stream().anyMatch(t -> t.getStatus() == TicketStatus.ISSUED)) {
            log.warn("Booking {} đã được xuất vé từ trước!", bookingId);
            throw new AppException(ErrorCode.BOOKING_ALREADY_ISSUED);
        }

        for (Ticket ticket : booking.getTickets()) {
            if (ticket.getStatus() == TicketStatus.RESERVED) {
                Long sequence = ticketRepository.getNextTicketSequence();
                String ticketNumber = TICKET_PREFIX + String.format("%010d", sequence);
                ticket.setTicketNumber(ticketNumber);
                ticket.setStatus(TicketStatus.ISSUED);
            }
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        log.info("Đã xuất vé thành công cho Booking: {}", booking.getPnrCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ETicketEmailModel> getTicketsByBookingId(UUID bookingId) {
        return bookingDtoMapper.mapToTickets(getBookingEntityById(bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ETicketEmailModel> getTicketsByPnrCode(String pnrCode) {
        return getTicketsByBookingId(getBookingEntityByPnr(pnrCode).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingClientByPnrAndContactEmail(BookingSearchRequest request) {
        Booking booking = bookingRepository.findByPnrCodeAndContactEmail(request.getPnrCode(), request.getContactEmail())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        BookingDetailResponse response = bookingDtoMapper.mapToBookingDetailResponse(booking);
        response.setTransactions(transactionService.getClientTransactionsByBookingId(booking.getId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingSummaryResponse> getMyBookings(String filter, int page, int size) {
        UserResponse user = userService.getMyInfo();
        Pageable pageable = PageRequest.of(page - 1, size);
        LocalDateTime now = LocalDateTime.now();
        Page<Booking> bookingPage;

        List<BookingStatus> successStatuses = List.of(BookingStatus.PAID, BookingStatus.CONFIRMED);
        List<BookingStatus> cancelStatuses = List.of(BookingStatus.CANCELLED, BookingStatus.REFUNDED);

        switch (filter.toUpperCase()) {
            case "UPCOMING":
                bookingPage = bookingRepository.findUpcomingBookings(user.getId(), successStatuses, now, pageable);
                break;
            case "COMPLETED":
                bookingPage = bookingRepository.findCompletedBookings(user.getId(), successStatuses, now, pageable);
                break;
            case "CANCELLED":
                bookingPage = bookingRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(user.getId(), cancelStatuses, pageable);
                break;
            default:
                bookingPage = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        }

        List<BookingSummaryResponse> content = bookingPage.getContent().stream()
                .map(bookingDtoMapper::mapToBookingSummaryResponse).toList();

        return PageResponse.<BookingSummaryResponse>builder()
                .currentPage(page)
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalElements(bookingPage.getTotalElements())
                .data(content)
                .build();
    }

    @Override
    @Transactional
    public void cancelUnpaidBooking(UUID bookingId) {
        UserResponse user = userService.getMyInfo();
        Booking booking = getBookingEntityById(bookingId);

        if (booking.getUserId() == null || !booking.getUserId().equals(user.getId())) {
            log.warn("User {} cố tình hủy Booking {} của người khác!", user.getId(), bookingId);
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            log.error("Không thể hủy vé vì trạng thái hiện tại là: {}", booking.getStatus());
            throw new AppException(ErrorCode.CANNOT_CANCEL_BOOKING);
        }

        processCancellationLogic(booking);
        log.info("User {} đã tự hủy thành công Booking {}", user.getId(), booking.getPnrCode());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminBookingSummaryResponse> searchBookingsForAdmin(AdminBookingSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Specification<Booking> spec = BookingSpecification.getSearchSpec(request);
        Page<Booking> bookingPage = bookingRepository.findAll(spec, pageable);

        List<AdminBookingSummaryResponse> content = bookingPage.getContent().stream()
                .map(bookingDtoMapper::mapToAdminBookingSummaryResponse).toList();

        return PageResponse.<AdminBookingSummaryResponse>builder()
                .currentPage(page)
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalElements(bookingPage.getTotalElements())
                .data(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminBookingDetailResponse getBookingDetailsForAdmin(UUID id) {
        Booking booking = getBookingEntityById(id);
        return AdminBookingDetailResponse.builder()
                .baseDetails(bookingDtoMapper.mapToBookingDetailResponse(booking))
                .transactions(transactionService.getTransactionsByBookingId(id))
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .userId(booking.getUserId())
                .build();
    }

    @Override
    @Transactional
    public void forceCancelAndRefund(UUID bookingId) {
        Booking booking = getBookingEntityById(bookingId);

        validateDepartureTime(booking);

        BookingStatus currentStatus = booking.getStatus();
        boolean isPaidAndNeedsRefund = (currentStatus == BookingStatus.PAID || currentStatus == BookingStatus.CONFIRMED);
        if (isPaidAndNeedsRefund) {
            long hoursUntilDeparture = calculateHoursUntilFirstFlight(booking);

            if (hoursUntilDeparture < 24) {
                throw new AppException(ErrorCode.NON_REFUNDABLE);
            }

            Transaction transaction = transactionService.findSuccessfulVnpayTransactionByBookingId(bookingId);
            if (transaction != null) {
                try {
                    vnPayRefundService.processRealRefund(booking, transaction);
                    log.info("Đã hoàn tiền thành công VNPay cho PNR: {}", booking.getPnrCode());
                } catch (Exception e) {
                    log.error("Lỗi khi hoàn tiền qua VNPay cho PNR {}: {}", booking.getPnrCode(), e.getMessage());
                    throw new AppException(ErrorCode.REFUND_FAILED);
                }
            } else {
                log.warn("Booking {} trạng thái PAID/CONFIRMED nhưng không có giao dịch VNPay hợp lệ.", booking.getPnrCode());
            }
        }
        processCancellationLogic(booking);
        log.info("Admin đã ép hủy thành công PNR: {}", booking.getPnrCode());
    }

    @Override
    public void resendBookingEmail(UUID bookingId) {
        BookingDetailResponse booking = getBookingById(bookingId);
        BookingStatus status = booking.getStatus();

        if (status != BookingStatus.CONFIRMED && status != BookingStatus.PAID) {
            log.warn("Không thể gửi lại email. BookingId: {}, PNR: {}, Status: {}", bookingId, booking.getPnrCode(), status);
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }
        emailService.sendBookingConfirmationEmail(booking);
        log.info("Gửi lại email thành công. BookingId: {}, PNR: {}", bookingId, booking.getPnrCode());
    }

    @Override
    public void updatePassengerInfo(String pnrCode, UUID passengerId, UpdatePassengerRequest request) {
        Booking booking = getValidMyBookingForModification(pnrCode);
        Passenger passenger = booking.getPassengers().stream()
                .filter(p -> p.getId().equals(passengerId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PASSENGER_NOT_FOUND));

        // 3. Tìm chặng bay đầu tiên để lấy thời gian cất cánh làm mốc tính tuổi
        BookingFlight firstFlight = booking.getBookingFlights().stream()
                .min(Comparator.comparing(BookingFlight::getSegmentNo))
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        PassengerType currentType = PassengerUtils.calculatePassengerType(
                passenger.getDateOfBirth(),
                firstFlight.getOriginDepartureTime()
        );

        PassengerType newType = PassengerUtils.calculatePassengerType(
                request.getDob(),
                firstFlight.getOriginDepartureTime()
        );

        if (currentType != newType) {
            log.warn("User {} cố gắng đổi ngày sinh làm thay đổi loại hành khách. PNR: {}, PassengerID: {}",
                    booking.getUserId(), pnrCode, passengerId);
            throw new AppException(ErrorCode.CANNOT_CHANGE_PASSENGER_TYPE);
        }
        passenger.setFirstName(request.getFirstName());
        passenger.setLastName(request.getLastName());
        passenger.setDateOfBirth(request.getDob());
        passenger.setGender(request.getGender());
        bookingRepository.save(booking);
        log.info("Đã cập nhật thông tin hành khách thành công cho PNR: {}", pnrCode);
    }

    private Booking getValidMyBookingForModification(String pnrCode) {
        UserResponse user = userService.getMyInfo();
        Booking booking = getBookingEntityByPnr(pnrCode);
        if (booking.getUserId() == null || !booking.getUserId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        // B. Chỉ cho phép sửa vé đã thanh toán thành công hoặc đã xuất vé
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PAID) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }
        // C. Kiểm tra Buffer Time (Không cho sửa nếu máy bay sắp cất cánh)
        validateDepartureTime(booking);

        return booking;
    }


    private void processCancellationLogic(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.getTickets().forEach(ticket -> ticket.setStatus(TicketStatus.CANCELLED));

        int totalPassengers = booking.getPassengers().size();
        for (BookingFlight bookingFlight : booking.getBookingFlights()) {
            try {
                flightClassService.increaseSeats(bookingFlight.getFlightClassId(), totalPassengers);
            } catch (Exception e) {
                log.error("Lỗi khi trả ghế chuyến bay {}: {}", bookingFlight.getFlightClassId(), e.getMessage());
            }
        }
        bookingRepository.save(booking);
    }

    private void validateDepartureTime(Booking booking) {
        long hoursUntilDeparture = calculateHoursUntilFirstFlight(booking);
        if (hoursUntilDeparture < CANCEL_BUFFER_HOURS) {
            throw new AppException(ErrorCode.DEPARTURE_TIME_TOO_CLOSE);
        }
    }

    private long calculateHoursUntilFirstFlight(Booking booking) {
        BookingFlight firstFlight = booking.getBookingFlights().stream()
                .min(Comparator.comparing(BookingFlight::getSegmentNo))
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        return ChronoUnit.HOURS.between(LocalDateTime.now(), firstFlight.getOriginDepartureTime());
    }
}