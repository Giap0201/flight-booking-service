package com.utc.flight_booking_service.booking.response.share;

import com.utc.flight_booking_service.booking.enums.PassengerType;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.inventory.entity.FlightClassType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ETicketEmailModel {
    String ticketNumber;
    String pnrCode;
    TicketStatus status;

    String passengerFullName;
    PassengerType passengerType;

    String flightNumber;
    String origin;
    String destination;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;

    FlightClassType classType;
    String seatNumber; // BỔ SUNG: Số ghế (Có thể null nếu chưa chọn)
    String baggageAllowance; // BỔ SUNG: Ghi chú hành lý cơ bản

    // 5. Giá vé (Tùy chọn: Có hệ thống ẩn giá vé trên E-Ticket để tránh lộ khi mua hộ)
    BigDecimal totalAmount;
}