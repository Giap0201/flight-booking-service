package com.utc.flight_booking_service.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewPasswordEmailRequest {

    private String to;
    private String name;
    private String newPassword;
}