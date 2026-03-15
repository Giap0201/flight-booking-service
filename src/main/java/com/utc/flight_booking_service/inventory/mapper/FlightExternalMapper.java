package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.AviationFlightDTO;
import com.utc.flight_booking_service.inventory.entity.Airline;
import com.utc.flight_booking_service.inventory.entity.Airport;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightStatus;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Random;

@Mapper(componentModel = "spring", imports = FlightStatus.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightExternalMapper {
    static final int DAYS_TO_SHIFT = 30;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flightNumber", source = "flight.iata")
    @Mapping(target = "status", expression = "java(FlightStatus.SCHEDULED)")
    @Mapping(target = "departureTime", source = "departure.scheduled")
    @Mapping(target = "arrivalTime", source = "arrival.scheduled")
    @Mapping(target = "airline.code", source = "airline.iata")
    @Mapping(target = "aircraft.code", source = "aircraft.iata")
    @Mapping(target = "origin.code", source = "departure.iata")
    @Mapping(target = "destination.code", source = "arrival.iata")
    @Mapping(target = "aviationFlightId", expression = "java(generateAviationId(dto))")
    Flight toEntity(AviationFlightDTO dto);

    @AfterMapping
    default void fillMissingData(@MappingTarget Flight flight) {
        String[] TOP_GLOBAL_AIRLINES = {"AA", "DL", "UA", "EK", "QR", "LH", "SQ"};

        String[] TOP_GLOBAL_AIRPORTS = {"ATL", "DXB", "LHR", "HND", "LAX", "SIN", "CDG"};

        Random random = new Random();

        // 1. Nếu API không trả về hãng bay -> Random
        if (flight.getAirline() == null || flight.getAirline().getCode() == null || flight.getAirline().getCode().trim().isEmpty()) {
            Airline airline = new Airline();
            airline.setCode(TOP_GLOBAL_AIRLINES[random.nextInt(TOP_GLOBAL_AIRLINES.length)]);
            flight.setAirline(airline);
        }

        // 2. Nếu API không trả về sân bay đi -> Random 1 Hub quốc tế
        if (flight.getOrigin() == null || flight.getOrigin().getCode() == null || flight.getOrigin().getCode().trim().isEmpty()) {
            Airport origin = new Airport();
            origin.setCode(TOP_GLOBAL_AIRPORTS[random.nextInt(TOP_GLOBAL_AIRPORTS.length)]);
            flight.setOrigin(origin);
        }

        // 3. Nếu API không trả về sân bay đến -> Random 1 Hub quốc tế
        if (flight.getDestination() == null || flight.getDestination().getCode() == null || flight.getDestination().getCode().trim().isEmpty()) {
            Airport dest = new Airport();
            dest.setCode(TOP_GLOBAL_AIRPORTS[random.nextInt(TOP_GLOBAL_AIRPORTS.length)]);
            flight.setDestination(dest);
        }
    }

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
