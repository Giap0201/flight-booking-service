package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.request.AirportRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Airport;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AirportMapper {
    Airport toEntity(AirportRequestDTO request);

    AirportResponseDTO toResponseDTO(Airport airport);

    List<AirportResponseDTO> toResponseDTOList(List<Airport> airports);

    void updateEntity(AirportRequestDTO request, @MappingTarget Airport airport);
}
