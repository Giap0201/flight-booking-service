package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.response.AirportResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Airport;
import com.utc.flight_booking_service.inventory.mapper.AirportMapper;
import com.utc.flight_booking_service.inventory.repository.AirportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirportService implements IAirportService {
    AirportRepository airportRepository;
    AirportMapper airportMapper;

    @Override
    public PageResponse<AirportResponseDTO> getAllAirports(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Airport> airportPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            airportPage = airportRepository.findAll(pageable);
        } else {
            airportPage = airportRepository.searchAirports(keyword, pageable);
        }

        return PageResponse.<AirportResponseDTO>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(airportPage.getTotalPages())
                .totalElements(airportPage.getTotalElements())
                .data(airportMapper.toResponseDTOList(airportPage.getContent()))
            .build();
    }
}
