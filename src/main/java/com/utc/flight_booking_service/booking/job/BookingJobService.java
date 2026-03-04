package com.utc.flight_booking_service.booking.job;

import com.utc.flight_booking_service.booking.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingJobService {
    BookingService bookingService;

    @Scheduled(cron = "0 * * * * ?")
    public void cancelExpiredBookings() { // Đổi tên hàm cho chuẩn nghiệp vụ nhé (không phải token)
        log.info("[CRON JOB] Bắt đầu quét các Booking hết hạn...");
        bookingService.cancelExpiredBookings();
        log.info("[CRON JOB] Đã quét và dọn dẹp xong!");
    }
}

