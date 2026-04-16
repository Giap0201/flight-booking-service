package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.dto.request.AirportRequestDTO;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public AirportResponseDTO createAirport(AirportRequestDTO request) {
        // Kiểm tra xem mã sân bay (IATA) đã tồn tại chưa
        if (airportRepository.existsById(request.getCode())) {
            throw new AppException(ErrorCode.AIRPORT_EXISTED);
        }

        Airport airport = airportMapper.toEntity(request);
        return airportMapper.toResponseDTO(airportRepository.save(airport));
    }

    @Override
    @Transactional
    public AirportResponseDTO updateAirport(String code, AirportRequestDTO request) {
        // Tìm sân bay hiện tại, nếu không thấy ném lỗi 404
        Airport airport = airportRepository.findById(code)
                .orElseThrow(() -> new AppException(ErrorCode.AIRPORT_NOT_FOUND));

        // Sử dụng MapStruct để cập nhật các field từ DTO vào Entity
        airportMapper.updateEntity(request, airport);

        return airportMapper.toResponseDTO(airportRepository.save(airport));
    }

    @Override
    @Transactional
    public void deleteAirport(String code) {
        // Kiểm tra tồn tại trước khi xóa để ném đúng mã lỗi
        if (!airportRepository.existsById(code)) {
            throw new AppException(ErrorCode.AIRPORT_NOT_FOUND);
        }
        airportRepository.deleteById(code);
    }
}
