package com.utc.flight_booking_service.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utc.flight_booking_service.inventory.dto.AviationResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "AVIATIONSTACK_KEY=c3f668ddb5220b8fcc9070f3e78e9fec")
public class AviationParsingTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Parse thành công JSON từ Aviationstack sang DTO")
    void shouldParseJsonToDto() throws IOException {
        // Đọc file từ resources
        var jsonFile = new ClassPathResource("data/mock_flights.json").getInputStream();

        // Thực hiện parse
        AviationResponseDTO response = objectMapper.readValue(jsonFile, AviationResponseDTO.class);

        // Kiểm tra dữ liệu
        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(1);

        var flight = response.getData().get(0);
        assertThat(flight.getFlightStatus()).isEqualTo("scheduled");
        assertThat(flight.getDeparture().getIata()).isEqualTo("HAN");
        assertThat(flight.getAirline().getIata()).isEqualTo("VN");
        assertThat(flight.getFlight().getIata()).isEqualTo("VN213");
    }
}
