package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.inventory.repository.FlightClassRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        // 1. Chuẩn bị dữ liệu: Chuyển sang sử dụng UUID thực tế
        // Lưu ý: ID này phải tồn tại trong DB test của cậu
        UUID targetId = UUID.fromString("87a7c31f-2ade-4729-a7e6-9070c5467f2d");

        // 2. Thiết lập công cụ điều phối luồng
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1); // Chốt kích hoạt đồng loạt
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads); // Chốt đợi hoàn thành

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 3. Tạo 2 luồng cùng thực hiện trừ ghế đồng thời
        for (int i = 0; i < numberOfThreads; i++) {
            executor.execute(() -> {
                try {
                    startLatch.await(); // Đợi hiệu lệnh "chạy"
                    flightClassService.decreaseSeats(targetId, 1);
                    successCount.incrementAndGet();
                } catch (AppException e) {
                    // Bắt lỗi 3006 (UPDATE_SEAT_FAILED) do xung đột Version (Optimistic Lock)
                    if (e.getErrorCode().getCode() == 3006) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown(); // Báo cáo đã hoàn thành việc thực thi luồng
                }
            });
        }

        // 4. Bấm nút "CHẠY" cho cả 2 luồng cùng lúc
        startLatch.countDown();

        // Đợi tối đa 5 giây để các luồng xử lý xong thay vì ngủ cứng 2 giây
        boolean completed = finishLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // 5. Kiểm tra kết quả
        System.out.println("Số lần thành công: " + successCount.get());
        System.out.println("Số lần thất bại (do xung đột version): " + failureCount.get());

        // Kỳ vọng: Trong môi trường khóa lạc quan, chỉ 1 luồng được commit thành công
        assertEquals(1, successCount.get(), "Chỉ được phép 1 giao dịch thành công");
        assertEquals(1, failureCount.get(), "Phải có 1 giao dịch thất bại do khóa lạc quan");
    }
}