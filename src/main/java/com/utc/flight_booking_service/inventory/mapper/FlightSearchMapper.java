package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.CheapestDateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightClassDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import com.utc.flight_booking_service.inventory.repository.projection.ICheapestPriceProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightSearchMapper {

    @Mapping(target = "airlineName", source = "airline.name")
    @Mapping(target = "origin", source = "origin.code")
    @Mapping(target = "destination", source = "destination.code")
    @Mapping(target = "classes", source = "flightClasses")
    FlightSearchResponseDTO toResponseDTO(Flight flight);

    @Mapping(target = "className", source = "classType")
    FlightClassDTO toFlightClassDTO(FlightClass flightClass);

    List<FlightSearchResponseDTO> toResponseDTOList(List<Flight> flights);

    @Mapping(target = "date", source = "departureDate")
    @Mapping(target = "minPrice", source = "minPrice")
    CheapestDateResponseDTO toCheapestDTO(ICheapestPriceProjection projection);

    List<CheapestDateResponseDTO> toCheapestDTOList(List<ICheapestPriceProjection> projections);
}
