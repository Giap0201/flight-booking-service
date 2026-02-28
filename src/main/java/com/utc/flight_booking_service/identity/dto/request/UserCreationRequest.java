package com.utc.flight_booking_service.identity.dto.request;

import jakarta.validation.constraints.Email;
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

public class UserCreationRequest {

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 50, message = "PASSWORD_INVALID")
    private String password;

    @NotBlank(message = "FULLNAME_REQUIRED")
    @Size(min = 2, max = 50, message = "FULLNAME_INVALID")
    private String fullName;

    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(
            regexp = "^(0[0-9]{9})$",
            message = "PHONE_INVALID"
    )
    private String phone;

}







