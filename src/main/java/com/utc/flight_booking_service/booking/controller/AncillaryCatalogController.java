package com.utc.flight_booking_service.booking.controller;

import com.utc.flight_booking_service.booking.request.AncillaryCatalogCreationRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogSearchRequest;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogUpdateRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminAncillaryCatalogResponse;
import com.utc.flight_booking_service.booking.response.client.AncillaryCatalogResponse;
import com.utc.flight_booking_service.booking.service.AncillaryCatalogService;
import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.common.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/ancillary-catalogs")
public class AncillaryCatalogController {
    AncillaryCatalogService ancillaryCatalogService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AdminAncillaryCatalogResponse> createAncillaryCatalog (@Valid @RequestBody AncillaryCatalogCreationRequest request){
        return ApiResponse.<AdminAncillaryCatalogResponse>builder()
                .result(ancillaryCatalogService.createAncillaryCatalog(request))
                .build();

    }

    @GetMapping("/search")
    ApiResponse<PageResponse<AdminAncillaryCatalogResponse>> searchAncillaryCatalog (@ModelAttribute AncillaryCatalogSearchRequest request,
                                                                                     @RequestParam(defaultValue = "1") int page,
                                                                                     @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<PageResponse<AdminAncillaryCatalogResponse>>builder()
                .result(ancillaryCatalogService.getAllAncillaryCatalog(request, page, size))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<AdminAncillaryCatalogResponse> getAncillaryCatalog(@PathVariable UUID id){
        return ApiResponse.<AdminAncillaryCatalogResponse>builder()
                .result(ancillaryCatalogService.getAncillaryCatalog(id))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    ApiResponse<AdminAncillaryCatalogResponse> updateAncillaryCatalog(@PathVariable  UUID id, @Valid @RequestBody AncillaryCatalogUpdateRequest request){
        return ApiResponse.<AdminAncillaryCatalogResponse>builder()
                .result(ancillaryCatalogService.updateAncillaryCatalog(id, request))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    ApiResponse<Void> deleteAncillaryCatalog(@PathVariable UUID id){
        ancillaryCatalogService.deleteAncillaryCatalog(id);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping
    ApiResponse<List<AncillaryCatalogResponse>> getAllAncillaryCatalog(){
        return ApiResponse.<List<AncillaryCatalogResponse>>builder()
                .result(ancillaryCatalogService.getAllAncillaryCatalog())
                .build();
    }
}
