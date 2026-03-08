package com.utc.flight_booking_service.booking.response.share;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContactResponse {
    String name;
    String email;
    String phone;
}
