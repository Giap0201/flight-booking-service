package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.AviationFlightDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightExternalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flightNumber", source = "flight.iata")
    @Mapping(target = "status", source = "flightStatus")
    @Mapping(target = "departureTime", source = "departure.scheduled")
    @Mapping(target = "arrivalTime", source = "arrival.scheduled")
    @Mapping(target = "airline.code", source = "airline.iata")
    @Mapping(target = "aircraft.code", source = "aircraft.iata")
    @Mapping(target = "origin.code", source = "departure.iata")
    @Mapping(target = "destination.code", source = "arrival.iata")
    @Mapping(target = "aviationFlightId", expression = "java(generateAviationId(dto))")
    Flight toEntity(AviationFlightDTO dto);

    default String generateAviationId(AviationFlightDTO dto) {
        if (dto.getAirline() == null || dto.getFlight() == null) return null;
        return dto.getAirline().getIata() + dto.getFlight().getNumber() + "_" + dto.getDeparture().getScheduled();
    }

    default LocalDateTime mapStringToLocalDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // Bước 1: Parse chuỗi có chứa offset (+00:00) thành OffsetDateTime
            // Bước 2: Chuyển OffsetDateTime thành LocalDateTime để tương thích với Database
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception e) {
            // Fallback (dự phòng) trường hợp API trả về format không có dấu +
            return LocalDateTime.parse(value);
        }
    }
}
