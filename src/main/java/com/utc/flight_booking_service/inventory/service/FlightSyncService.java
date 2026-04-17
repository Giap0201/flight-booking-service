package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.client.AviationstackClient;
import com.utc.flight_booking_service.inventory.dto.response.AviationResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.mapper.FlightExternalMapper;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightSyncService implements IFlightSyncService {
    AviationstackClient aviationClient;
    FlightExternalMapper flightMapper;
    IFlightEnrichmentService enrichmentService;
    FlightRepository flightRepository;

    @NonFinal
    @Value("${aviationstack.api.key}")
    String apiKey;

    @Override
    @Transactional
    public String fetchAndMapFlights() {
        // 1. Gọi API lấy dữ liệu (Mặc định lấy tuyến SGN-HAN để tối ưu quota Free Plan)
        AviationResponseDTO response = aviationClient.getFlights(apiKey, "SGN", "HAN", 100);

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return "Không có dữ liệu từ API";
        }

        List<Flight> flightsToSave = new ArrayList<>();
        int successCount = 0;

        for (var dto : response.getData()) {
            try {
                Flight flight = flightMapper.toEntity(dto);

                if (flight.getAviationFlightId() == null || flight.getAviationFlightId().isEmpty()) {
                    log.warn("Chuyến bay thiếu mã định danh, bỏ qua.");
                    continue;
                }

                // Tiến hành UPSERT
                flightRepository.findByAviationFlightId(flight.getAviationFlightId())
                        .ifPresentOrElse(
                                existingFlight -> {
                                    existingFlight.setStatus(flight.getStatus());
                                    existingFlight.setDepartureTime(flight.getDepartureTime());
                                    existingFlight.setArrivalTime(flight.getArrivalTime());
                                    flightsToSave.add(existingFlight);
                                },
                                () -> {
                                    var classes = enrichmentService.enrichFlight(flight, flight.getAircraft());
                                    flight.setFlightClasses(classes);
                                    flightsToSave.add(flight);
                                }
                        );
                successCount++;
            } catch (Exception e) {
                log.error("Lỗi dòng dữ liệu (Airline: {}, Flight: {}): {}",
                        dto.getAirline().getIata(), dto.getFlight().getIata(), e.getMessage());
            }
        }

        if (!flightsToSave.isEmpty()) {
            flightRepository.saveAll(flightsToSave);
        }

        log.info("Đồng bộ hoàn tất: {} bản ghi đã xử lý", successCount);
        return "Thành công";
    }
}