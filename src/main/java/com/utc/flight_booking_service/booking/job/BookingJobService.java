package com.utc.flight_booking_service.booking.job;

import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.service.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingJobService {
    BookingService bookingService;

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void cancelExpiredBookings() {
        log.info("[CRON JOB] Bắt đầu quét các Booking hết hạn...");
        bookingService.cancelExpiredBookings();
        log.info("[CRON JOB] Đã quét và dọn dẹp xong!");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void hardDeleteOldCancelledBookings() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        log.info("Bắt đầu dọn dẹp các Booking rác bị HỦY từ trước ngày {}", thirtyDaysAgo);
        int deletedCount = bookingService.deleteByStatusAndCreatedAtBefore(BookingStatus.CANCELLED, thirtyDaysAgo);
        log.info("Đã dọn dẹp thành công {} Booking rác và giải phóng Database!", deletedCount);
    }
}

