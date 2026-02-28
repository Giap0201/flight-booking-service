package com.utc.flight_booking_service.identity.dto.response;

import com.utc.flight_booking_service.identity.domain.entities.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    String email;

    String fullName;

    String phone;

    Set<Role> roles;

}
