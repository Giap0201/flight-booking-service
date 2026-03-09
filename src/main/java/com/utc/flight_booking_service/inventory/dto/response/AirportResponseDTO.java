package com.utc.flight_booking_service.inventory.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirportResponseDTO {
    String code;       // Mã IATA (VD: HAN) [cite: 1]
    String name;       // Tên sân bay [cite: 1]
    String cityCode;   // Mã thành phố [cite: 1]
    String countryCode;// Mã quốc gia [cite: 1]
}
