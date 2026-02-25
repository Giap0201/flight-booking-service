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
    static final int DAYS_TO_SHIFT = 30;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flightNumber", source = "flight.iata")
    @Mapping(target = "status", constant = "SCHEDULED")
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
            return OffsetDateTime.parse(value)
                    .toLocalDateTime()
                    .plusDays(DAYS_TO_SHIFT); // Cộng thêm 30 ngày vào tương lai
        } catch (Exception e) {
            return LocalDateTime.parse(value).plusDays(DAYS_TO_SHIFT); // Fallback cũng cộng thêm
        }
    }
}
