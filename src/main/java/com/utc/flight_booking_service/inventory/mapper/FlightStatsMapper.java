package com.utc.flight_booking_service.inventory.mapper;


import com.utc.flight_booking_service.inventory.dto.response.FlightStatisticsResponseDTO;
import com.utc.flight_booking_service.inventory.repository.projection.IFlightStatsProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FlightStatsMapper {

    @Mapping(target = "totalFlightsToday", source = "totalFlights")
    @Mapping(target = "totalSeatsCapacity", source = "totalSeats")
    @Mapping(target = "availableSeatsLeft", source = "availableSeats")
    @Mapping(target = "soldSeats", expression = "java(projection.getTotalSeats() - projection.getAvailableSeats())")
    @Mapping(target = "occupancyRate", source = "projection", qualifiedByName = "calculateRate")
    FlightStatisticsResponseDTO toStatisticsDTO(IFlightStatsProjection projection);

    @Named("calculateRate")
    default double calculateRate(IFlightStatsProjection p) {
        if (p.getTotalSeats() == null || p.getTotalSeats() == 0) return 0.0;
        double sold = p.getTotalSeats() - p.getAvailableSeats();
        return Math.round((sold / p.getTotalSeats() * 100) * 100.0) / 100.0;
    }
}
