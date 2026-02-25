package com.utc.flight_booking_service.inventory.client;

import com.utc.flight_booking_service.inventory.dto.AviationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "aviationstack-client", url = "${aviationstack.api.url}")
public interface AviationstackClient {

    @GetMapping("/flights")
    AviationResponseDTO getFlights(
            @RequestParam("access_key") String apiKey,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    );
}
