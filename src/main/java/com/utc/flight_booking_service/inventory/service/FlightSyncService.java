package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.client.AviationstackClient;
import com.utc.flight_booking_service.inventory.dto.AviationFlightDTO;
import com.utc.flight_booking_service.inventory.dto.AviationResponseDTO;
import com.utc.flight_booking_service.inventory.entity.*;
import com.utc.flight_booking_service.inventory.mapper.FlightExternalMapper;
import com.utc.flight_booking_service.inventory.repository.AircraftRepository;
import com.utc.flight_booking_service.inventory.repository.AirlineRepository;
import com.utc.flight_booking_service.inventory.repository.AirportRepository;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightSyncService {
    AviationstackClient aviationClient;
    FlightExternalMapper flightMapper;
    FlightEnrichmentService enrichmentService;
    AirlineRepository airlineRepository;
    AirportRepository airportRepository;
    AircraftRepository aircraftRepository;
    FlightRepository flightRepository;

    @NonFinal
    @Value("${aviationstack.api.key}")
    private String apiKey;

    public void fetchAndMapFlights() {
        AviationResponseDTO response = aviationClient.getFlights(apiKey, 100);

        if (response == null || response.getData() == null) return;

        int successCount = 0;
        int failCount = 0;

        for (var dto : response.getData()) {
            // BỌC TRY-CATCH CHO TỪNG CHUYẾN BAY
            try {
                if (dto.getDeparture() == null || dto.getArrival() == null || dto.getAirline() == null) continue;
                if (dto.getDeparture().getIata() == null || dto.getArrival().getIata() == null) continue;

                String aviationFlightId = flightMapper.generateAviationId(dto);
                Optional<Flight> existingFlightOpt = flightRepository.findByAviationFlightId(aviationFlightId);

                if (existingFlightOpt.isPresent()) {
                    // CẬP NHẬT (UPSERT)
                    Flight existingFlight = existingFlightOpt.get();
                    existingFlight.setStatus(dto.getFlightStatus());

                    flightRepository.save(existingFlight);
                    successCount++;
                } else {
                    // TẠO MỚI (INSERT)
                    Airline airline = getOrCreateAirline(dto.getAirline());
                    Airport origin = getOrCreateAirport(
                            dto.getDeparture().getIata(),
                            dto.getDeparture().getAirport(),
                            dto.getDeparture().getTimezone()
                    );
                    Airport destination = getOrCreateAirport(
                            dto.getArrival().getIata(),
                            dto.getArrival().getAirport(),
                            dto.getArrival().getTimezone()
                    );
                    Aircraft aircraft = getOrCreateAircraft(dto.getAircraft());

                    Flight flight = flightMapper.toEntity(dto);
                    flight.setAirline(airline);
                    flight.setOrigin(origin);
                    flight.setDestination(destination);
                    flight.setAircraft(aircraft);

                    List<FlightClass> flightClasses = enrichmentService.enrichFlight(flight, aircraft);
                    flight.setFlightClasses(flightClasses);

                    flightRepository.save(flight);
                    successCount++;
                }
            } catch (Exception e) {
                // NẾU LỖI 1 CHUYẾN, CHỈ LOG RA VÀ CHẠY TIẾP VÒNG LẶP CHO CHUYẾN KHÁC
                System.err.println("Lỗi khi lưu chuyến bay " + flightMapper.generateAviationId(dto) + ": " + e.getMessage());
                failCount++;
            }
        }

        System.out.println("Tổng kết Sync: Thành công " + successCount + ", Thất bại " + failCount);
    }

    // ==========================================
    // LOGIC FIND OR CREATE
    // ==========================================

    private Airport getOrCreateAirport(String code, String name, String timezone) {
        String safeCode = (code != null && !code.trim().isEmpty()) ? code : "UNK";

        return airportRepository.findById(safeCode).orElseGet(() -> {
            Airport newAirport = new Airport();
            newAirport.setCode(safeCode);
            newAirport.setName(name != null ? name : "Airport " + safeCode);
            newAirport.setCityCode(safeCode);
            newAirport.setCountryCode("VN"); // Default
            newAirport.setTimezone(timezone);
            return airportRepository.save(newAirport);
        });
    }

    private Airline getOrCreateAirline(AviationFlightDTO.AirlineDTO dto) {
        String code = (dto != null && dto.getIata() != null && !dto.getIata().trim().isEmpty()) ? dto.getIata() : "UNK";
        return airlineRepository.findById(code).orElseGet(() -> {
            Airline newAirline = new Airline();
            newAirline.setCode(code);
            newAirline.setName(dto != null && dto.getName() != null ? dto.getName() : "Unknown Airline");
            return airlineRepository.save(newAirline);
        });
    }

    private Aircraft getOrCreateAircraft(AviationFlightDTO.AircraftDTO dto) {
        String code;
        if (dto != null && dto.getIata() != null && !dto.getIata().trim().isEmpty()) {
            code = dto.getIata();
        } else {
            java.util.List<String> commonAircrafts = java.util.List.of("A320", "A321", "A350", "B777", "B787");
            code = commonAircrafts.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(commonAircrafts.size()));
        }

        return aircraftRepository.findById(code).orElseGet(() -> {
            Aircraft newAircraft = new Aircraft();
            newAircraft.setCode(code);
            newAircraft.setName("Airbus/Boeing " + code);

            // 3. Phân bổ số ghế logic hơn dựa theo loại máy bay
            if (code.equals("B787") || code.equals("A350") || code.equals("B777")) {
                newAircraft.setTotalEconomySeats(250); // Máy bay thân rộng (Nhiều ghế hơn)
                newAircraft.setTotalBusinessSeats(40);
            } else {
                newAircraft.setTotalEconomySeats(180); // Máy bay thân hẹp (A320, A321)
                newAircraft.setTotalBusinessSeats(16);
            }

            return aircraftRepository.save(newAircraft);
        });
    }
}

