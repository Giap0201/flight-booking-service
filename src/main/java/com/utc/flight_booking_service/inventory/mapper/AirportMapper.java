package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Airport;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirportMapper {
    AirportResponseDTO toResponseDTO(Airport airport);
    List<AirportResponseDTO> toResponseDTOList(List<Airport> airports);
}
