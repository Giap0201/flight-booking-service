package com.utc.flight_booking_service.booking.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
    @NotBlank(message = "CONTACT_NAME_REQUIRED")
    String contactName;

    @NotBlank(message = "CONTACT_EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID_FORMAT")
    String contactEmail;

    @NotBlank(message = "CONTACT_PHONE_REQUIRED")
    @Pattern(regexp = "^\\d{10,15}$", message = "PHONE_INVALID_FORMAT")
    String contactPhone;

    @Builder.Default
    String currency = "VND";

    UUID userId;

    @NotEmpty(message = "FLIGHTS_REQUIRED")
    @Valid
    List<BookingFlightRequest> flights;

    @NotEmpty(message = "PASSENGERS_REQUIRED")
    @Valid
    List<PassengerRequest> passengers;
}