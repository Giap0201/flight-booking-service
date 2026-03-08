package com.utc.flight_booking_service.booking.request;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminBookingSearchRequest {
    String pnrCode;
    String contactEmail;
    String contactPhone;
    BookingStatus status;
    LocalDateTime fromDate;
    LocalDateTime toDate;
}
