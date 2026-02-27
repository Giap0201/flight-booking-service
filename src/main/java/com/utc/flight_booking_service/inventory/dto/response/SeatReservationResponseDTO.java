package com.utc.flight_booking_service.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatReservationResponseDTO {
    Long flightClassId;
    Integer amountReserved;
    Integer availableSeatsLeft;
}
