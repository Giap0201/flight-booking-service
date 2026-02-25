package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.AviationFlightDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightExternalMapper {

    @Mapping(target = "flightNumber", source = "flight.iata")
    @Mapping(target = "status", source = "flightStatus")
    @Mapping(target = "departureTime", source = "departure.scheduled")
    @Mapping(target = "arrivalTime", source = "arrival.scheduled")
    // Mapping các quan hệ thông qua mã code
    @Mapping(target = "airline.code", source = "airline.iata")
    @Mapping(target = "aircraft.code", source = "aircraft.iata")
    @Mapping(target = "origin.code", source = "departure.iata")
    @Mapping(target = "destination.code", source = "arrival.iata")
    // Logic tạo aviationFlightId duy nhất
    @Mapping(target = "aviationFlightId", expression = "java(generateAviationId(dto))")
    Flight toEntity(AviationFlightDTO dto);

    default String generateAviationId(AviationFlightDTO dto) {
        if (dto.getAirline() == null || dto.getFlight() == null) return null;
        return dto.getAirline().getIata() + dto.getFlight().getNumber() + "_" + dto.getDeparture().getScheduled();
    }
}
