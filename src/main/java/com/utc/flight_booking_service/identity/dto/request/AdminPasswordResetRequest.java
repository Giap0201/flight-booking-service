package com.utc.flight_booking_service.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class AdminPasswordResetRequest {
    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 50, message = "PASSWORD_INVALID")
    String newPassword;
}
