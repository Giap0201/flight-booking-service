package com.utc.flight_booking_service.booking.repository;

import com.utc.flight_booking_service.booking.entity.Ticket;
import com.utc.flight_booking_service.dashboard.dto.response.RouteTicketCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    @Query(value = "SELECT nextval('ticket_number_seq')", nativeQuery = true)
    Long getNextTicketSequence();

    // trả về số vé đã phát hành của 1 chuyến bay
    @Query("SELECT t.flightId AS flightId, COUNT(t.id) AS ticketCount " +
            "FROM Ticket t " +
            "WHERE t.status = 'ISSUED' " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY t.flightId")
    List<RouteTicketCount> countTicketsByFlightId(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    //tra ve tong so ve da được phát hành
    @Query("SELECT COUNT(t.id) " +
            "FROM Ticket t " +
            "WHERE t.status = 'ISSUED' " +
            "AND t.createdAt BETWEEN :startDate AND :endDate")
    long countTotalIssuedTickets(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
