package com.utc.flight_booking_service.inventory;

import com.utc.flight_booking_service.inventory.repository.FlightRepository;
import com.utc.flight_booking_service.inventory.service.FlightSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSyncRunner implements CommandLineRunner {

    private final FlightSyncService flightSyncService;
    private final FlightRepository flightRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==========================================");
        System.out.println("BẮT ĐẦU ĐỒNG BỘ DỮ LIỆU TỪ AVIATIONSTACK...");

        // Kích hoạt service
        flightSyncService.fetchAndMapFlights();

        System.out.println("ĐỒNG BỘ HOÀN TẤT!");

        // In ra số lượng chuyến bay đang có trong Database
        long totalFlights = flightRepository.count();
        System.out.println("-> Tổng số chuyến bay trong Database hiện tại: " + totalFlights);
        System.out.println("==========================================");
    }
}
