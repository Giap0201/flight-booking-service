package com.utc.flight_booking_service.booking.response.client;

import com.utc.flight_booking_service.booking.enums.PassengerType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PassengerTicketResponse {
    UUID passengerId;
    String firstName;
    String lastName;
    PassengerType type;
    List<TicketDetailResponse> tickets;
}
