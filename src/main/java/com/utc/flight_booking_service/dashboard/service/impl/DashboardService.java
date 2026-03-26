package com.utc.flight_booking_service.dashboard.service.impl;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.repository.BookingRepository;
import com.utc.flight_booking_service.booking.repository.TicketRepository;
import com.utc.flight_booking_service.dashboard.dto.DailyRevenueResponse;
import com.utc.flight_booking_service.dashboard.dto.DashboardSummaryResponse;
import com.utc.flight_booking_service.dashboard.dto.response.RouteTicketCount;
import com.utc.flight_booking_service.dashboard.dto.response.TopRouteResponse;
import com.utc.flight_booking_service.dashboard.service.IDashboardService;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class DashboardService implements IDashboardService {
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;


    private final List<BookingStatus> SUCCESS_STATUSES = List.of(BookingStatus.PAID, BookingStatus.CONFIRMED);

    private final List<BookingStatus> CANCEL_STATUSES = List.of(BookingStatus.CANCELLED, BookingStatus.REFUNDED);

    @PreAuthorize("hasRole('ADMIN')")
    public DashboardSummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal revenue = bookingRepository.calculateTotalRevenue(SUCCESS_STATUSES, start, end);
        long successBookings = bookingRepository.countBookingsByStatus(SUCCESS_STATUSES, start, end);
        long cancelledBookings = bookingRepository.countBookingsByStatus(CANCEL_STATUSES, start, end);
        long totalTicketIssued = ticketRepository.countTotalIssuedTickets(start, end);

        return DashboardSummaryResponse.builder()
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .totalBookings(successBookings)
                .totalCancelledBookings(cancelledBookings)
                .totalTicketsIssued(totalTicketIssued)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<DailyRevenueResponse> getRevenueChart(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        return bookingRepository.getDailyRevenueChart(start, end);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TopRouteResponse> getTop5Routes(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);


        List<RouteTicketCount> flightCounts = ticketRepository.countTicketsByFlightId(start, end);
        if (flightCounts.isEmpty()) return Collections.emptyList();


        List<UUID> flightIds = flightCounts.stream().map(RouteTicketCount::getFlightId).distinct().toList();


        List<Flight> masterFlights = flightRepository.findAllById(flightIds);


        Map<String, Long> routeMap = new HashMap<>();
        long totalTickets = 0;

        for (RouteTicketCount fc : flightCounts) {
            Flight flight = masterFlights.stream()
                    .filter(f -> f.getId().equals(fc.getFlightId()))
                    .findFirst().orElse(null);

            if (flight != null && flight.getOrigin() != null && flight.getDestination() != null) {


                String routeCode = flight.getOrigin().getCityCode() + " - " + flight.getDestination().getCityCode();

                routeMap.put(routeCode, routeMap.getOrDefault(routeCode, 0L) + fc.getTicketCount());
                totalTickets += fc.getTicketCount();
            }
        }


        final long finalTotal = totalTickets;
        return routeMap.entrySet().stream()
                .map(entry -> {
                    double percentage = (finalTotal == 0) ? 0 : Math.round(((double) entry.getValue() / finalTotal) * 1000.0) / 10.0;
                    return new TopRouteResponse(entry.getKey(), entry.getValue(), percentage);
                })
                .sorted(Comparator.comparing(TopRouteResponse::getTicketCount).reversed())
                .limit(5)
                .toList();
    }
}
