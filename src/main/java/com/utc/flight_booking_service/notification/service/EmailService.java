package com.utc.flight_booking_service.notification.service;

import com.utc.flight_booking_service.notification.dto.NewPasswordEmailRequest;

public interface EmailService {
    void sendNewPasswordEmail(NewPasswordEmailRequest request);
}
