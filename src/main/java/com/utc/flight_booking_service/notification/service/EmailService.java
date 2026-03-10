package com.utc.flight_booking_service.notification.service;

import com.utc.flight_booking_service.booking.response.client.BookingDetailResponse;
import com.utc.flight_booking_service.notification.dto.NewPasswordEmailRequest;

public interface EmailService {
    void sendNewPasswordEmail(NewPasswordEmailRequest request);

    void sendBookingConfirmationEmail(BookingDetailResponse bookingData);
}
