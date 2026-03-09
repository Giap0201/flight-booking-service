package com.utc.flight_booking_service.booking.mapper;

import com.utc.flight_booking_service.booking.entity.AncillaryCatalog;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogCreationRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogUpdateRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminAncillaryCatalogResponse;
import com.utc.flight_booking_service.booking.response.client.AncillaryCatalogResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AncillaryCatalogMapper {
    @Mapping(target = "id", ignore = true)
    AncillaryCatalog toAncillaryCatalog(AncillaryCatalogCreationRequest request);

    AdminAncillaryCatalogResponse toAdminAncillaryCatalog(AncillaryCatalog ancillaryCatalog);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAncillaryCatalog(@MappingTarget AncillaryCatalog ancillaryCatalog, AncillaryCatalogUpdateRequest request);

    AncillaryCatalogResponse toAncillaryCatalogResponse(AncillaryCatalog ancillaryCatalog);
}
