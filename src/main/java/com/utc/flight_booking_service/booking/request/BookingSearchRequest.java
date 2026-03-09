package com.utc.flight_booking_service.booking.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingSearchRequest {
    @NotBlank(message = "PNR_REQUIRED")
    String pnrCode;

    @NotBlank(message = "CONTACT_EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID_FORMAT")
    String contactEmail;
}
