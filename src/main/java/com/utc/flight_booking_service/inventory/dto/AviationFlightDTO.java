package com.utc.flight_booking_service.inventory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationFlightDTO {
    @JsonProperty("flight_date")
    String flightDate;

    @JsonProperty("flight_status")
    String flightStatus;

    DepartureDTO departure;
    ArrivalDTO arrival;
    AirlineDTO airline;
    FlightDetailDTO flight;
    AircraftDTO aircraft;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DepartureDTO {
        String airport;
        String timezone;
        String iata;     // Map to Airport.code
        String scheduled; // ISO 8601
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArrivalDTO {
        String airport;
        String iata;     // Map to Airport.code
        String scheduled;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AirlineDTO {
        String name;
        String iata; // Map to Airline.code
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightDetailDTO {
        String number;
        String iata; // Map to Flight.flightNumber (VD: VN123)
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AircraftDTO {
        String iata; // Map to Aircraft.code
    }
}
