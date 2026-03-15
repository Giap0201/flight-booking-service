package com.utc.flight_booking_service.booking.service;

import com.utc.flight_booking_service.booking.entity.AncillaryCatalog;
import com.utc.flight_booking_service.booking.enums.AncillaryCatalogStatus;
import com.utc.flight_booking_service.booking.mapper.AncillaryCatalogMapper;
import com.utc.flight_booking_service.booking.repository.AncillaryCatalogRepository;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogCreationRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogSearchRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogUpdateRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminAncillaryCatalogResponse;
import com.utc.flight_booking_service.booking.response.client.AncillaryCatalogResponse;
import com.utc.flight_booking_service.booking.specification.AncillaryCatalogSpecification;
import com.utc.flight_booking_service.common.PageResponse;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AncillaryCatalogServiceImpl implements AncillaryCatalogService {
    AncillaryCatalogRepository ancillaryCatalogRepository;
    AncillaryCatalogMapper ancillaryCatalogMapper;

    @Override
    public AdminAncillaryCatalogResponse createAncillaryCatalog(AncillaryCatalogCreationRequest request) {
        if (ancillaryCatalogRepository.existsByCode(request.getCode()))
            throw new AppException(ErrorCode.ANCILLARY_CATALOG_CODE_EXISTED);
        if (ancillaryCatalogRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.ANCILLARY_CATALOG_NAME_EXISTED);

        AncillaryCatalog ancillaryCatalog = ancillaryCatalogMapper.toAncillaryCatalog(request);
        AncillaryCatalog savedAncillaryCatalog = ancillaryCatalogRepository.save(ancillaryCatalog);
        return ancillaryCatalogMapper.toAdminAncillaryCatalog(savedAncillaryCatalog);
    }

    @Override
    public AdminAncillaryCatalogResponse getAncillaryCatalog(UUID id) {
        AncillaryCatalog ancillaryCatalog = ancillaryCatalogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANCILLARY_CATALOG_NOT_FOUND));
        return ancillaryCatalogMapper.toAdminAncillaryCatalog(ancillaryCatalog);
    }

    @Override
    public PageResponse<AdminAncillaryCatalogResponse> getAllAncillaryCatalog(AncillaryCatalogSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<AncillaryCatalog> spec = AncillaryCatalogSpecification.getAncillaryCatalogSpecification(request);
        Page<AncillaryCatalog> ancillaryCatalogPage = ancillaryCatalogRepository.findAll(spec, pageable);
        List<AdminAncillaryCatalogResponse> content = ancillaryCatalogPage.getContent()
                .stream().map(ancillaryCatalogMapper::toAdminAncillaryCatalog).toList();
        return PageResponse.<AdminAncillaryCatalogResponse>builder()
                .currentPage(page)
                .totalElements(ancillaryCatalogPage.getTotalElements())
                .totalPages(ancillaryCatalogPage.getTotalPages())
                .pageSize(ancillaryCatalogPage.getSize())
                .data(content)
                .build();
    }

    @Override
    public AdminAncillaryCatalogResponse updateAncillaryCatalog(UUID id, AncillaryCatalogUpdateRequest request) {
        AncillaryCatalog ancillaryCatalog = ancillaryCatalogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANCILLARY_CATALOG_NOT_FOUND));

        if (!ancillaryCatalog.getName().equals(request.getName()) &&
                ancillaryCatalogRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ANCILLARY_CATALOG_NAME_EXISTED);
        }
        ancillaryCatalogMapper.updateAncillaryCatalog(ancillaryCatalog, request);
        return ancillaryCatalogMapper.toAdminAncillaryCatalog(ancillaryCatalog);
    }

    @Override
    public void deleteAncillaryCatalog(UUID id) {
        AncillaryCatalog ancillaryCatalog = ancillaryCatalogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANCILLARY_CATALOG_NOT_FOUND));
        ancillaryCatalog.setStatus(AncillaryCatalogStatus.INACTIVE);
    }

    @Override
    public List<AncillaryCatalogResponse> getAllAncillaryCatalog() {
        return ancillaryCatalogRepository.findByStatus(AncillaryCatalogStatus.ACTIVE)
                .stream()
                .map(ancillaryCatalogMapper::toAncillaryCatalogResponse)
                .toList();
    }
}
