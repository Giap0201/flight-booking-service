package com.utc.flight_booking_service.identity.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserCreationRequest {

    String email;

    String password;

    String fullName;

    String phone;

}







