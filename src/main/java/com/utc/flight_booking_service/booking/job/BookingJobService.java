package com.utc.flight_booking_service.booking.job;

import com.utc.flight_booking_service.booking.entity.Booking;
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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingJobService {
    BookingService bookingService;

    @Scheduled(cron = "0 * * * * ?")
    public void cancelPendingBookings() {
        List<Booking> pendingBookings = bookingService.getExpiredBookingsByStatus(BookingStatus.PENDING);
        int count = 0;
        log.info("Bắt đầu huỷ {} vé đang ở trạng thái pending", pendingBookings.size());
        if (!pendingBookings.isEmpty()) {
            log.info("[BOOKING JOB] Dọn dẹp {} vé PENDING rác...", pendingBookings.size());
            for (Booking booking : pendingBookings) {
                try {
                    bookingService.cancelSingleBookingBySystem(booking.getId());
                    count ++;
                } catch (Exception e) {
                    log.error("Lỗi khi hủy vé rác PNR {}: {}", booking.getPnrCode(), e.getMessage());
                }
            }
        }
        log.info("Huỷ thành công {} vé đang ở trạng thái pending", count);

    }

//    @Scheduled(cron = "0 0 2 * * ?")
//    @Transactional
//    public void hardDeleteOldCancelledBookings() {
//        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
//        log.info("Bắt đầu dọn dẹp các Booking rác bị HỦY từ trước ngày {}", thirtyDaysAgo);
//        int deletedCount = bookingService.deleteByStatusAndCreatedAtBefore(BookingStatus.CANCELLED, thirtyDaysAgo);
//        log.info("Đã dọn dẹp thành công {} Booking rác và giải phóng Database!", deletedCount);
//    }
}

