package com.utc.flight_booking_service.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightClassDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    String className;
    Double basePrice;
    Integer availableSeats;
}
