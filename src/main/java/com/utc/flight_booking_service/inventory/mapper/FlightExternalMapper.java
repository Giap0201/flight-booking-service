package com.utc.flight_booking_service.inventory.mapper;

import com.utc.flight_booking_service.inventory.dto.response.AviationFlightDTO;
import com.utc.flight_booking_service.inventory.entity.*;
import com.utc.flight_booking_service.inventory.repository.AircraftRepository;
import com.utc.flight_booking_service.inventory.repository.AirlineRepository;
import com.utc.flight_booking_service.inventory.repository.AirportRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Mapper(componentModel = "spring", imports = {FlightStatus.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FlightExternalMapper {

    @Autowired
    protected AirlineRepository airlineRepository;
    @Autowired
    protected AirportRepository airportRepository;
    @Autowired
    protected AircraftRepository aircraftRepository;

    private static final String[] TOP_GLOBAL_AIRLINES = {"AA", "DL", "UA", "EK", "QR", "LH", "SQ", "VN", "QH"};
    private static final String[] TOP_GLOBAL_AIRPORTS = {"ATL", "DXB", "LHR", "HND", "LAX", "SIN", "CDG", "HAN", "SGN"};

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flightNumber", source = "flight.iata")
    @Mapping(target = "status", expression = "java(FlightStatus.SCHEDULED)")
    @Mapping(target = "departureTime", source = "departure.scheduled")
    @Mapping(target = "arrivalTime", source = "arrival.scheduled")
    @Mapping(target = "airline", source = "airline")
    @Mapping(target = "origin", source = "departure")
    @Mapping(target = "destination", source = "arrival")
    @Mapping(target = "aircraft", source = "aircraft")
// Bổ sung System.currentTimeMillis() vào ID
    @Mapping(target = "aviationFlightId", expression = "java(dto.getFlight() != null ? " +
            "( (dto.getFlight().getNumber() != null ? dto.getFlight().getNumber() : \"\") + " +
            "  (dto.getFlight().getIata() != null ? dto.getFlight().getIata() : \"\") + " +
            "  (dto.getFlight().getIcao() != null ? dto.getFlight().getIcao() : \"\") + " +
            "  \"_\" + System.currentTimeMillis() ) : null)")
    public abstract Flight toEntity(AviationFlightDTO dto);

    // AfterMapping bắt buộc phải có đủ (dto, flight) để MapStruct không bị lỗi FilerException
    @AfterMapping
    protected void linkCustomLogic(AviationFlightDTO dto, @MappingTarget Flight.FlightBuilder flightBuilder) {
        // Lấy dữ liệu tạm thời từ DTO để tính toán vì lúc này Flight chưa build xong
        if (dto.getDeparture() != null && dto.getDeparture().getScheduled() != null &&
                dto.getArrival() != null && dto.getArrival().getScheduled() != null) {

            // 1. Parse thời gian từ chuỗi gốc của DTO
            LocalDateTime departureTime = mapStringToLocalDateTime(dto.getDeparture().getScheduled());
            LocalDateTime arrivalTime = mapStringToLocalDateTime(dto.getArrival().getScheduled());

            if (departureTime != null && arrivalTime != null) {
                // 2. Sinh số ngày ngẫu nhiên
                int randomDays = java.util.concurrent.ThreadLocalRandom.current().nextInt(3, 31);

                // 3. Gán lại giá trị đã cộng ngày vào Builder
                flightBuilder.departureTime(departureTime.plusDays(randomDays));
                flightBuilder.arrivalTime(arrivalTime.plusDays(randomDays));

                // Log thử để kiểm tra trong Console
                System.out.println("===> Đã cộng " + randomDays + " ngày cho chuyến bay: " + dto.getFlight().getIata());
            }
        }
    }

    // ==========================================
    // LOGIC MAP AIRLINE (RANDOM FALLBACK)
    // ==========================================
    protected Airline mapAirline(AviationFlightDTO.AirlineDTO dto) {
        String code = (dto != null && dto.getIata() != null && !dto.getIata().trim().isEmpty())
                ? dto.getIata()
                : TOP_GLOBAL_AIRLINES[ThreadLocalRandom.current().nextInt(TOP_GLOBAL_AIRLINES.length)];

        return airlineRepository.findById(code).orElseGet(() -> {
            Airline newAirline = Airline.builder()
                    .code(code)
                    .name(dto != null && dto.getName() != null ? dto.getName() : "Global Airline " + code)
                    .logoUrl("https://img.logo.dev/name/" + code + "?token=pk_fDBJo_JTRm2WiIRgffk4Yw")
                    .build();
            return airlineRepository.save(newAirline);
        });
    }

    // ==========================================
    // LOGIC MAP AIRPORT (RANDOM FALLBACK)
    // ==========================================
    protected Airport mapOrigin(AviationFlightDTO.DepartureDTO dto) {
        return getOrCreateAirport(dto != null ? dto.getIata() : null, dto != null ? dto.getAirport() : null, dto != null ? dto.getTimezone() : null);
    }

    protected Airport mapDestination(AviationFlightDTO.ArrivalDTO dto) {
        return getOrCreateAirport(dto != null ? dto.getIata() : null, dto != null ? dto.getAirport() : null, dto != null ? dto.getTimezone() : null);
    }

    private Airport getOrCreateAirport(String code, String name, String timezone) {
        String safeCode = (code != null && !code.trim().isEmpty())
                ? code
                : TOP_GLOBAL_AIRPORTS[ThreadLocalRandom.current().nextInt(TOP_GLOBAL_AIRPORTS.length)];

        return airportRepository.findById(safeCode).orElseGet(() -> {
            Airport newAirport = Airport.builder()
                    .code(safeCode)
                    .name(name != null ? name : "International Airport " + safeCode)
                    .cityCode(safeCode)
                    .countryCode("VN")
                    .timezone(timezone != null ? timezone : "Asia/Ho_Chi_Minh")
                    .build();
            return airportRepository.save(newAirport);
        });
    }

    // ==========================================
    // LOGIC MAP AIRCRAFT (RANDOM FALLBACK)
    // ==========================================
    protected Aircraft mapAircraft(AviationFlightDTO.AircraftDTO dto) {
        String code = (dto != null && dto.getIata() != null) ? dto.getIata() :
                java.util.List.of("A320", "A321", "A350", "B787").get(ThreadLocalRandom.current().nextInt(4));

        return aircraftRepository.findById(code).orElseGet(() -> {
            Aircraft ac = Aircraft.builder().code(code).name("Airbus/Boeing " + code).build();
            if (java.util.List.of("B787", "A350").contains(code)) {
                ac.setTotalEconomySeats(250); ac.setTotalBusinessSeats(40);
            } else {
                ac.setTotalEconomySeats(180); ac.setTotalBusinessSeats(16);
            }
            return aircraftRepository.save(ac);
        });
    }

    protected LocalDateTime mapStringToLocalDateTime(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return java.time.OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception e) {
            return java.time.LocalDateTime.parse(value);
        }
    }
}