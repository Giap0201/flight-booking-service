package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.dto.request.FlightSearchRequestDTO;
import com.utc.flight_booking_service.inventory.dto.request.FlightValidationRequestDTO;
import com.utc.flight_booking_service.inventory.dto.response.CheapestDateResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightDetailResponseDTO;
import com.utc.flight_booking_service.inventory.dto.response.FlightSearchResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import com.utc.flight_booking_service.inventory.mapper.FlightMapper;
import com.utc.flight_booking_service.inventory.mapper.FlightSearchMapper;
import com.utc.flight_booking_service.inventory.repository.FlightClassRepository;
import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import com.utc.flight_booking_service.inventory.repository.FlightSpecification;
import com.utc.flight_booking_service.inventory.repository.projection.ICheapestPriceProjection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightSearchService implements IFlightSearchService{
    FlightRepository flightRepository;
    FlightClassRepository flightClassRepository;
    FlightSearchMapper flightSearchMapper;
    FlightMapper flightMapper;

    @Cacheable(value = "flight_search",
            key = "'SEARCH:' + #request.origin + ':' + #request.destination + ':' + #request.date")
    public List<FlightSearchResponseDTO> searchAvailableFlights(FlightSearchRequestDTO request) {
        Specification<Flight> spec = FlightSpecification.searchFlights(
                request.getOrigin(),
                request.getDestination(),
                request.getDate(),
                request.getPassengers()
        );

        List<Flight> flights = flightRepository.findAll(spec);

        if (flights.isEmpty()) {
            throw new AppException(ErrorCode.FLIGHT_NOT_FOUND);
        }

        return flightSearchMapper.toResponseDTOList(flights);
    }

    @Override
    public FlightDetailResponseDTO getFlightDetail(UUID id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FLIGHT_NOT_FOUND));

        return flightMapper.toDetailResponse(flight);
    }

    @Override
    public boolean validateFlightForBooking(FlightValidationRequestDTO request) {
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElse(null);

        if (flight == null || !"SCHEDULED".equalsIgnoreCase(String.valueOf(flight.getStatus()))) {
            return false; // Chuyến bay đã bị hủy hoặc không tồn tại
        }

        FlightClass flightClass = flightClassRepository.findById(request.getFlightClassId())
                .orElse(null);

        if (flightClass == null || flightClass.getAvailableSeats() < request.getNumberOfPassengers()) {
            return false;
        }

        // Kiểm tra xem giá có bị Admin thay đổi trong lúc khách đang nhập liệu không
        // Sử dụng compareTo để so sánh chính xác dữ liệu BigDecimal
        return flightClass.getBasePrice().compareTo(request.getExpectedPrice()) == 0;
    }

    @Override
    public List<FlightSearchResponseDTO> getFlightsByIds(List<UUID> ids) {
        List<Flight> flights = flightRepository.findAllByIdIn(ids);

        return flightSearchMapper.toResponseDTOList(flights);
    }

    @Override
    public List<CheapestDateResponseDTO> getCheapestPricesInMonth(String origin, String destination, int year, int month) {
        //Thiết lập khoảng thời gian (Tuân thủ múi giờ UTC)
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDateTime startOfMonth = firstDay.atStartOfDay();
        LocalDateTime endOfMonth = firstDay.plusMonths(1).atStartOfDay().minusNanos(1);

        List<ICheapestPriceProjection> projections = flightRepository.findCheapestPricesByMonth(
                origin.toUpperCase(), destination.toUpperCase(), startOfMonth, endOfMonth);

        return flightSearchMapper.toCheapestDTOList(projections);
    }
}
