package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.dto.request.FlightManualRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.FlightUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.PriceUpdateRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightStatisticsResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.PriceUpdateResponseDTO;
import com.utc.flight_booking_service.inventory.entity.*;
import com.utc.flight_booking_service.inventory.mapper.FlightClassMapper;
import com.utc.flight_booking_service.inventory.mapper.FlightMapper;
import com.utc.flight_booking_service.inventory.mapper.FlightSearchMapper;
import com.utc.flight_booking_service.inventory.mapper.FlightStatsMapper;
import com.utc.flight_booking_service.inventory.repository.*;
import com.utc.flight_booking_service.inventory.repository.projection.IFlightStatsProjection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightService implements IFlightService{
    AircraftRepository aircraftRepository;
    AirlineRepository airlineRepository;
    AirportRepository airportRepository;
    FlightRepository flightRepository;
    FlightClassRepository flightClassRepository;
    FlightMapper flightMapper;
    FlightClassMapper flightClassMapper;
    FlightStatsMapper flightStatsMapper;
    FlightSearchMapper flightSearchMapper;
    IFlightEnrichmentService flightEnrichmentService;

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public FlightUpdateResponseDTO updateFlightStatus(UUID flightId, FlightUpdateRequestDTO request) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        if (request.getDepartureTime() != null) flight.setDepartureTime(request.getDepartureTime());
        if (request.getArrivalTime() != null) flight.setArrivalTime(request.getArrivalTime());
        if (request.getStatus() != null) flight.setStatus(FlightStatus.valueOf(request.getStatus().toUpperCase()));

        return flightMapper.toUpdateResponse(flightRepository.save(flight));
    }

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public PriceUpdateResponseDTO updatePrice(UUID flightClassId, PriceUpdateRequestDTO request) {
        FlightClass flightClass = flightClassRepository.findById(flightClassId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        flightClass.setBasePrice(request.getBasePrice());
        return flightClassMapper.toPriceResponse(flightClassRepository.save(flightClass));
    }

    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public UUID createManualFlight(FlightManualRequestDTO request) {
        // 1. Kiểm tra các thực thể Master Data liên quan
        Airline airline = airlineRepository.findById(request.getAirlineCode())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        Airport origin = airportRepository.findById(request.getOriginCode())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        Airport destination = airportRepository.findById(request.getDestinationCode())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        Aircraft aircraft = aircraftRepository.findById(request.getAircraftCode())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // 2. Khởi tạo thực thể Flight
        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .airline(airline)
                .origin(origin)
                .destination(destination)
                .aircraft(aircraft)
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .status(FlightStatus.SCHEDULED)
                // Tạo ID duy nhất để tránh trùng với dữ liệu sync
                .aviationFlightId("MANUAL_" + request.getFlightNumber() + "_" + System.currentTimeMillis())
                .build();

        // 3. Tự động sinh 4 hạng vé từ Enrichment Service
        List<FlightClass> flightClasses = flightEnrichmentService.enrichFlight(flight, aircraft);
        flight.setFlightClasses(flightClasses);

        // 4. Lưu vào DB và trả về UUID mới
        return flightRepository.save(flight).getId();
    }

    @Override
    public FlightStatisticsResponseDTO getTodayStatistics() {
        //Xác định mốc thời gian bắt đầu và kết thúc ngày theo UTC
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        IFlightStatsProjection stats = flightRepository.getRawStatistics(startOfDay, endOfDay);

        if (stats.getTotalFlights() == 0) {
            return new FlightStatisticsResponseDTO();
        }
        return flightStatsMapper.toStatisticsDTO(stats);
    }

    @Override
    public PageResponse<FlightSearchResponseDTO> getAllFlights(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Flight> flightPage = flightRepository.findAll(pageable);

        return PageResponse.<FlightSearchResponseDTO>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(flightPage.getTotalPages())
                .totalElements(flightPage.getTotalElements())
                .data(flightPage.getContent().stream()
                        .map(flightSearchMapper::toResponseDTO) // [cite: 145]
                        .toList())
                .build();
    }
}
