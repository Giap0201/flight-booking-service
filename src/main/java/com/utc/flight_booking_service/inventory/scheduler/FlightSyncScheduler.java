package com.utc.flight_booking_service.inventory.scheduler;

import com.utc.flight_booking_service.inventory.service.IFlightSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightSyncScheduler {

    private final IFlightSyncService flightSyncService;

    // Chay moi 1 phut
//    @Scheduled(cron = "0 * * * * ?")
    // Chạy vào lúc 2 giờ 0 phút 0 giây sáng, mỗi ngày
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncFlightsDaily() {
        log.info("Bắt đầu chạy Scheduled Job: Đồng bộ dữ liệu chuyến bay từ Aviationstack...");

        long startTime = System.currentTimeMillis();

        flightSyncService.fetchAndMapFlights();

        long endTime = System.currentTimeMillis();
        log.info("Hoàn tất Scheduled Job! Thời gian chạy: {} ms", (endTime - startTime));
    }
}