package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.AirlineResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Airline;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirlineMapper {
    AirlineResponseDTO toResponseDTO(Airline airline);
    List<AirlineResponseDTO> toResponseDTOList(List<Airline> airlines);
}
