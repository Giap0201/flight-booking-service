package com.utc.flight_booking_service.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEmailResponse {
    private String pnrCode;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;


    private List<FlightInfo> flights;


    private List<PassengerTicketInfo> passengers;


    @Data
    @Builder
    public static class FlightInfo {
        private String flightNumber;
        private String airlineName;
        private String airlineLogoUrl;
        private String originCode;
        private String originCity;
        private String destinationCode;
        private String destinationCity;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String classType;
    }

    @Data
    @Builder
    public static class PassengerTicketInfo {
        private String fullName;
        private String passengerType;
        private String ticketNumber;
        private String seatNumber;
        private BigDecimal baseFare;
        private BigDecimal taxAmount;
        private List<AncillaryInfo> ancillaries;
    }

    @Data
    @Builder
    public static class AncillaryInfo {
        private String name;
        private BigDecimal price;
    }
}
