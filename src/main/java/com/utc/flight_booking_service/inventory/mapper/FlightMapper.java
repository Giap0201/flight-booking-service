package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.FlightDetailResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AirlineMapper.class, AirportMapper.class})
public interface FlightMapper {
    FlightUpdateResponseDTO toUpdateResponse(Flight flight);

    @Mapping(target = "flightClasses", source = "flightClasses")
    FlightDetailResponseDTO toDetailResponse(Flight flight);
}
