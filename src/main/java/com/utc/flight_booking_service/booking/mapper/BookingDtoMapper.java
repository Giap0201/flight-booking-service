package com.utc.flight_booking_service.booking.mapper;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.entity.BookingFlight;
import com.utc.flight_booking_service.booking.entity.Ticket;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.client.*;
import com.utc.flight_booking_service.booking.response.share.ContactResponse;
import com.utc.flight_booking_service.booking.response.share.ETicketEmailModel;
import com.utc.flight_booking_service.inventory.dto.response.FlightPriceResponseDTO;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingDtoMapper {
    IFlightClassService flightClassService;

    public BookingSummaryResponse mapToBookingSummaryResponse(Booking booking) {
        BookingSummaryResponse summary = BookingSummaryResponse.builder()
                .id(booking.getId())
                .pnrCode(booking.getPnrCode())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                //Trung them 1 dong
                .passengerCount(booking.getPassengers() != null ? booking.getPassengers().size() : 0)
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
            //Trung them 2 dong
            summary.setArrivalTime(firstBookingFlight.getOriginArrivalTime());
            summary.setClassType(flightInfo.getClassType());
        }
        return summary;
    }

    public BookingDetailResponse mapToBookingDetailResponse(Booking booking) {
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

                        List<AncillaryItemResponse> ancillaries = Optional.ofNullable(booking.getBookingAncillaries())
                                .orElseGet(Collections::emptyList)
                                .stream()
                                .filter(anc -> anc.getPassenger().getId().equals(passenger.getId()))
                                .filter(anc -> anc.getBookingFlight().getFlightClassId().equals(ticket.getFlightClassId()))
                                .map(anc -> AncillaryItemResponse.builder()
                                        .catalogName(anc.getCatalog().getName())
                                        .type(anc.getCatalog().getType())
                                        .amount(anc.getAmount())
                                        .build())
                                .toList();

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
                                .ancillaries(ancillaries)
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

    public AdminBookingSummaryResponse mapToAdminBookingSummaryResponse(Booking booking) {
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

    public List<ETicketEmailModel> mapToTickets(Booking booking) {
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
}
