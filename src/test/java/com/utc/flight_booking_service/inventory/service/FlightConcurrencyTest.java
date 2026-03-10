package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.inventory.repository.FlightClassRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FlightConcurrencyTest {

    @Autowired
    IFlightClassService flightClassService;

    @Autowired
    FlightClassRepository flightClassRepository;

    @Test
    void testOptimisticLocking_ShouldFailForOneThread() throws InterruptedException {
        // 1. Chuẩn bị  dữ liệu: Lấy một hạng ghế đang có 100 chỗ
        String targetId = "87a7c31f-2ade-4729-a7e6-9070c5467f2d"; // Thay bằng ID thực tế trong DB của bạn
        int initialSeats = 100;

        // 2. Thiết lập công cụ điều phối luồng
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1); // Chốt để kích hoạt tất cả cùng lúc
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 3. Tạo 2 luồng cùng thực hiện trừ ghế
        for (int i = 0; i < numberOfThreads; i++) {
            executor.execute(() -> {
                try {
                    latch.await(); // Đợi hiệu lệnh "chạy"
                    // flightClassService.decreaseSeats(targetId, 1);
                    successCount.incrementAndGet();
                } catch (AppException e) {
                    // Bắt lỗi 3006 (UPDATE_SEAT_FAILED) đã cấu hình
                    if (e.getErrorCode().getCode() == 3006) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // 4. Bấm nút "CHẠY" cho cả 2 luồng cùng lúc
        latch.countDown();
        Thread.sleep(2000); // Đợi các luồng xử lý xong
        executor.shutdown();

        // 5. Kiểm tra kết quả
        System.out.println("Số lần thành công: " + successCount.get());
        System.out.println("Số lần thất bại (do xung đột version): " + failureCount.get());

        // Kỳ vọng: Chỉ có 1 luồng thành công, luồng còn lại phải dính Optimistic Locking
        assertEquals(1, successCount.get(), "Chỉ được phép 1 giao dịch thành công");
        assertEquals(1, failureCount.get(), "Phải có 1 giao dịch thất bại do khóa lạc quan");
    }
}
