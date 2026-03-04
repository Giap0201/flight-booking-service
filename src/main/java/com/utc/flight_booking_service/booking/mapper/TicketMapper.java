package com.utc.flight_booking_service.booking.mapper;

import com.utc.flight_booking_service.booking.entity.Ticket;
import com.utc.flight_booking_service.booking.response.ClientETicketResponse;
import com.utc.flight_booking_service.booking.response.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(source = "passenger.id", target = "passengerId")
    TicketResponse toTicketResponse(Ticket ticket);
}
