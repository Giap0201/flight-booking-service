package com.utc.flight_booking_service.booking.impl;

import com.utc.flight_booking_service.booking.PriceService;
import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.entity.Passenger;
import com.utc.flight_booking_service.booking.entity.Ticket;
import com.utc.flight_booking_service.booking.enums.PassengerType;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.booking.request.BookingFlightRequest;
import com.utc.flight_booking_service.booking.utils.PassengerUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PriceServiceImpl implements PriceService {

    static final BigDecimal CHILD_RATE = new BigDecimal("0.75"); // 75%
    static final BigDecimal INFANT_RATE = new BigDecimal("0.10"); // 10%
    static final BigDecimal TAX_RATE = new BigDecimal("0.10");    // 10%
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0");

    @Override
    public List<Ticket> calculateTickets(Booking booking,
                                         List<BookingFlightRequest> bookingFlightRequests) { // Bỏ tham số PassengerRequest đi

        List<Ticket> tickets = new ArrayList<>();
        for (BookingFlightRequest fReq : bookingFlightRequests) {

            // Mock giá & thuế (Sau này gọi FlightService)
            BigDecimal baseFare = new BigDecimal("1000000");
            BigDecimal taxRate = new BigDecimal("0.1");

            for (Passenger p : booking.getPassengers()) {
                PassengerType type = PassengerUtils.calculatePassengerType(p.getDateOfBirth(), LocalDateTime.now());

                BigDecimal finalBaseFare = applyAgePolicy(baseFare, type);
                BigDecimal taxAmount = finalBaseFare.multiply(taxRate);
                BigDecimal totalAmount = finalBaseFare.add(taxAmount);
                BigDecimal discountAmount = finalBaseFare.multiply(DISCOUNT_RATE);
                Ticket ticket = Ticket.builder()
                        .booking(booking)
                        .passenger(p)
                        .flightId(fReq.getFlightId())
                        .flightClassId(fReq.getFlightClassId())
                        .baseFare(finalBaseFare)
                        .taxAmount(taxAmount)
                        .totalAmount(totalAmount)
                        .status(TicketStatus.RESERVED)
                        .discountAmount(discountAmount)
                        .build();

                tickets.add(ticket);
            }
        }
        return tickets;
    }

    private BigDecimal applyAgePolicy(BigDecimal price, PassengerType passengerType) {
        if (passengerType == PassengerType.INFANT) return price.multiply(INFANT_RATE);
        if (passengerType == PassengerType.CHILD) return price.multiply(CHILD_RATE);
        return price;
    }
}
