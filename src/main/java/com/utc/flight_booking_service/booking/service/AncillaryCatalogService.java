package com.utc.flight_booking_service.booking.service;

import com.utc.flight_booking_service.booking.request.AdminBookingSearchRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogCreationRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogSearchRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogUpdateRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminAncillaryCatalogResponse;
import com.utc.flight_booking_service.booking.response.client.AncillaryCatalogResponse;
import com.utc.flight_booking_service.booking.response.share.PageResponse;

import java.util.List;
import java.util.UUID;

public interface AncillaryCatalogService {
    AdminAncillaryCatalogResponse createAncillaryCatalog(AncillaryCatalogCreationRequest request);

    AdminAncillaryCatalogResponse getAncillaryCatalog(UUID id);

    PageResponse<AdminAncillaryCatalogResponse> getAllAncillaryCatalog(AncillaryCatalogSearchRequest request, int page, int size);

    AdminAncillaryCatalogResponse updateAncillaryCatalog(UUID id, AncillaryCatalogUpdateRequest request);

    void deleteAncillaryCatalog(UUID id);

    List<AncillaryCatalogResponse> getAllAncillaryCatalog();

}
