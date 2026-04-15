package com.utc.flight_booking_service.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirportRequestDTO {
    @NotBlank(message = "AIRPORT_CODE_REQUIRED")
    @Size(min = 3, max = 3, message = "AIRPORT_CODE_INVALID")
    String code;

    @NotBlank(message = "AIRPORT_NAME_REQUIRED")
    String name;

    @NotBlank(message = "CITY_CODE_REQUIRED")
    String cityCode;

    String countryCode;
    String timezone;
}

