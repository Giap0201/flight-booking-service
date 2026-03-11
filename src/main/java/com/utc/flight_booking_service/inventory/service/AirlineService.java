package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.inventory.dto.response.AirlineResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Airline;
import com.utc.flight_booking_service.inventory.mapper.AirlineMapper;
import com.utc.flight_booking_service.inventory.repository.AirlineRepository;
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
public class AirlineService implements IAirlineService {
    AirlineRepository airlineRepository;
    AirlineMapper airlineMapper;

    @Override
    public PageResponse<AirlineResponseDTO> getAllAirlines(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Airline> airlinePage = airlineRepository.findAll(pageable);

        return PageResponse.<AirlineResponseDTO>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(airlinePage.getTotalPages())
                .totalElements(airlinePage.getTotalElements())
                .data(airlineMapper.toResponseDTOList(airlinePage.getContent()))
            .build();
    }
}
