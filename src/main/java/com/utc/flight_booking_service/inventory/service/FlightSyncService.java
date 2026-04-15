package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.client.AviationstackClient;
import com.utc.flight_booking_service.inventory.dto.response.AviationFlightDTO;
import com.utc.flight_booking_service.inventory.dto.response.AviationResponseDTO;
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
public class FlightSyncService implements IFlightSyncService{
    AviationstackClient aviationClient;
    FlightExternalMapper flightMapper;
    IFlightEnrichmentService enrichmentService;
    AirlineRepository airlineRepository;
    AirportRepository airportRepository;
    AircraftRepository aircraftRepository;
    FlightRepository flightRepository;

    @NonFinal
    @Value("${aviationstack.api.key}")
    private String apiKey;

    @Transactional // Đảm bảo toàn bộ batch save được thực thi trong 1 transaction duy nhất
    public String fetchAndMapFlights() {
        // Mặc định API limit là 100, tùy thuộc vào cấu hình plan
        AviationResponseDTO response = aviationClient.getFlights(apiKey, 100);

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return "Không có dữ liệu từ API";
        }

        int successCount = 0;
        int failCount = 0;

        // Khởi tạo danh sách chứa các entity cần update hoặc insert (Batch)
        List<Flight> flightsToSave = new ArrayList<>();

        for (var dto : response.getData()) {
            try {
                // 1. Kiểm tra tính toàn vẹn của dữ liệu gốc
                if (dto.getDeparture() == null || dto.getArrival() == null || dto.getAirline() == null || dto.getFlight() == null) continue;
                if (dto.getDeparture().getIata() == null || dto.getArrival().getIata() == null) continue;

                // 2. Tự tính toán ID đồng bộ trực tiếp tại Service
                String aviationFlightId = generateSyncId(dto);
                if (aviationFlightId == null) {
                    failCount++;
                    continue;
                }

                Optional<Flight> existingFlightOpt = flightRepository.findByAviationFlightId(aviationFlightId);

                if (existingFlightOpt.isPresent()) {
                    // CẬP NHẬT (UPSERT)
                    Flight existingFlight = existingFlightOpt.get();
                    existingFlight.setAviationFlightId(aviationFlightId);

                    // Xử lý an toàn (Null-safe) cho Flight Status
                    String rawStatus = dto.getFlightStatus();
                    if (rawStatus != null && !rawStatus.trim().isEmpty()) {
                        existingFlight.setStatus(FlightStatus.valueOf(rawStatus.toUpperCase()));
                    }

                    flightsToSave.add(existingFlight);
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
                    flight.setAviationFlightId(aviationFlightId);
                    flight.setAirline(airline);
                    flight.setOrigin(origin);
                    flight.setDestination(destination);
                    flight.setAircraft(aircraft);

                    List<FlightClass> flightClasses = enrichmentService.enrichFlight(flight, aircraft);
                    flight.setFlightClasses(flightClasses);

                    flightsToSave.add(flight);
                    successCount++;
                }
            } catch (Exception e) {
                // Log chi tiết, không làm gián đoạn vòng lặp
                System.err.println("Lỗi khi xử lý chuyến bay từ API: " + e.getMessage());
                failCount++;
            }
        }

        // 3. THỰC THI BATCH SAVE - Điểm mấu chốt cho High Performance
        if (!flightsToSave.isEmpty()) {
            flightRepository.saveAll(flightsToSave);
        }

        System.out.println("Tổng kết Sync: Thành công " + successCount + ", Thất bại " + failCount);
        return String.format("Đồng bộ hoàn tất: %d thành công, %d thất bại", successCount, failCount);
    }

    // ==========================================
    // LOGIC FIND OR CREATE
    // ==========================================

    private String generateSyncId(AviationFlightDTO dto) {
        if (dto.getAirline() == null || dto.getAirline().getIata() == null ||
                dto.getFlight() == null || dto.getFlight().getNumber() == null ||
                dto.getDeparture() == null || dto.getDeparture().getScheduled() == null) {
            return null;
        }
        return dto.getAirline().getIata() + dto.getFlight().getNumber() + "_" + dto.getDeparture().getScheduled();
    }

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
        String code = (dto != null && dto.getIata() != null && !dto.getIata().trim().isEmpty()) ?
                dto.getIata() : "UNK";

        Optional<Airline> existingAirlineOpt = airlineRepository.findById(code);

        if (existingAirlineOpt.isPresent()) {
            Airline airline = existingAirlineOpt.get();

            if (airline.getLogoUrl() == null || airline.getLogoUrl().isEmpty()) {
                String generatedLogoUrl = "https://img.logo.dev/iata/" + code + ".png?token=pk_fDBJo_JTRm2WiIRgffk4Yw";
                airline.setLogoUrl(generatedLogoUrl);
                return airlineRepository.save(airline);
            }

            return airline; // Trả về luôn nếu đã có logo
        } else {
            //TẠO MỚI NẾU CHƯA TỒN TẠI
            Airline newAirline = new Airline();
            newAirline.setCode(code);
            newAirline.setName(dto != null && dto.getName() != null ? dto.getName() : "Unknown Airline");

            newAirline.setLogoUrl("https://img.logo.dev/iata/" + code + ".png?token=pk_fDBJo_JTRm2WiIRgffk4Yw");

            return airlineRepository.save(newAirline);
        }
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

