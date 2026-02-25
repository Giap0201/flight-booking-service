package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.client.AviationstackClient;
import com.utc.flight_booking_service.inventory.dto.AviationResponseDTO;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.mapper.FlightExternalMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlightSyncService {
    AviationstackClient aviationClient;
    FlightExternalMapper flightMapper;

    @NonFinal
    @Value("${aviationstack.api.key}")
    private String apiKey;

    public List<Flight> fetchAndMapFlights() {
        AviationResponseDTO response = aviationClient.getFlights(apiKey, 100);

        return response.getData().stream()
                .filter(dto -> dto.getDeparture().getIata() != null && dto.getArrival().getIata() != null)
                .map(flightMapper::toEntity)
                .toList();
    }
}
