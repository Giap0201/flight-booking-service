package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FlightMapper {
    FlightUpdateResponseDTO toUpdateResponse(Flight flight);
}
