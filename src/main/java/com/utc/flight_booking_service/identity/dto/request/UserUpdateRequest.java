package com.utc.flight_booking_service.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank(message = "FULLNAME_REQUIRED")
    @Size(min = 2, max = 50, message = "FULLNAME_INVALID")
    String fullName;
    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(
            regexp = "^(0[0-9]{9})$",
            message = "PHONE_INVALID"
    )
    String phone;
}
