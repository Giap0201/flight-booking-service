package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.client.AviationstackClient;
import com.utc.flight_booking_service.inventory.dto.AviationAirportResponseDTO;
import com.utc.flight_booking_service.inventory.dto.AviationFlightDTO;
import com.utc.flight_booking_service.inventory.dto.AviationResponseDTO;
import com.utc.flight_booking_service.inventory.entity.*;
import com.utc.flight_booking_service.inventory.mapper.FlightExternalMapper;
import com.utc.flight_booking_service.inventory.repository.AircraftRepository;
import com.utc.flight_booking_service.inventory.repository.AirlineRepository;
import com.utc.flight_booking_service.inventory.repository.AirportRepository;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Transactional
    public void fetchAndMapFlights() {
        AviationResponseDTO response = aviationClient.getFlights(apiKey, 100);

        if (response == null || response.getData() == null) return;

        List<Flight> flightsToSave = new ArrayList<>();

        for (var dto : response.getData()) {
            if (dto.getDeparture() == null || dto.getArrival() == null || dto.getAirline() == null) continue;
            if (dto.getDeparture().getIata() == null || dto.getArrival().getIata() == null) continue;

            String aviationFlightId = flightMapper.generateAviationId(dto);
            Optional<Flight> existingFlightOpt = flightRepository.findByAviationFlightId(aviationFlightId);

            if (existingFlightOpt.isPresent()) {
                // NẾU ĐÃ TỒN TẠI (UPSERT): Cập nhật trạng thái hoặc giờ bay nếu cần
                Flight existingFlight = existingFlightOpt.get();
                existingFlight.setStatus(dto.getFlightStatus());
                flightsToSave.add(existingFlight);
            } else {
                // NẾU CHƯA TỒN TẠI (INSERT): Logic "Find or Create"
                Airline airline = getOrCreateAirline(dto.getAirline());
                Airport origin = getOrCreateAirport(dto.getDeparture().getIata());
                Airport destination = getOrCreateAirport(dto.getArrival().getIata());
                Aircraft aircraft = getOrCreateAircraft(dto.getAircraft());

                Flight flight = flightMapper.toEntity(dto);
                flight.setAirline(airline);
                flight.setOrigin(origin);
                flight.setDestination(destination);
                flight.setAircraft(aircraft);

                // Simulation Engine chỉ chạy cho chuyến bay mới
                List<FlightClass> flightClasses = enrichmentService.enrichFlight(flight, aircraft);
                flight.setFlightClasses(flightClasses);

                flightsToSave.add(flight);
            }
        }

        flightRepository.saveAll(flightsToSave);
    }

    // ==========================================
    // LOGIC FIND OR CREATE
    // ==========================================

    private Airport getOrCreateAirport(String code) {
        String safeCode = (code != null && !code.trim().isEmpty()) ? code : "UNK";

        return airportRepository.findById(safeCode).orElseGet(() -> {
            Airport newAirport = new Airport();
            newAirport.setCode(safeCode);
            newAirport.setName("Airport " + safeCode);

            // Gọi API phụ để lấy City và Country
            try {
                if (!"UNK".equals(safeCode)) {
                    AviationAirportResponseDTO res = aviationClient.getAirportDetails(apiKey, safeCode);

                    if (res != null && res.getData() != null && !res.getData().isEmpty()) {
                        // var detail lúc này sẽ mang kiểu AviationAirportResponseDTO.AirportDTO
                        var detail = res.getData().getFirst();

                        // Đã có thể gọi getAirportName() thoải mái mà không bị lỗi
                        newAirport.setName(detail.getAirportName() != null ? detail.getAirportName() : safeCode);
                        newAirport.setCityCode(detail.getCityIataCode());
                        newAirport.setCountryCode(detail.getCountryIso2());
                        newAirport.setTimezone(detail.getTimezone());
                    }
                }
            } catch (Exception e) {
                // Fallback im lặng nếu API airports bị lỗi
                System.err.println("Không thể fetch chi tiết cho sân bay: " + safeCode);
            }

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

