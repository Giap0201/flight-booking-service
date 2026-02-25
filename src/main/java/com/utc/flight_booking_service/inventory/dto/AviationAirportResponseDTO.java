package com.utc.flight_booking_service.inventory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationAirportResponseDTO {
    List<AirportDTO> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AirportDTO {
        @JsonProperty("airport_name")
        String airportName;

        @JsonProperty("iata_code")
        String iataCode;

        @JsonProperty("country_iso2")
        String countryIso2;

        @JsonProperty("city_iata_code")
        String cityIataCode;

        String timezone;
    }
}
