package com.utc.flight_booking_service.inventory.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightSearchRequestDTO {
    @NotBlank(message = "ORIGIN_REQUIRED")
    String origin;

    @NotBlank(message = "DESTINATION_REQUIRED")
    String destination;

    @NotNull(message = "DATE_REQUIRED")
    @FutureOrPresent(message = "DATE_INVALID")
    LocalDate date;

    @Min(value = 1, message = "PASSENGERS_INVALID")
    int passengers = 1;
}
