package com.utc.flight_booking_service.payment.job;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import com.utc.flight_booking_service.payment.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentJobService {

    PaymentService paymentService;
    BookingService bookingService;

    // QUÉT VÉ AWAITING_PAYMENT
    @Scheduled(cron = "0 * * * * ?")
    public void reconcileAwaitingPayments() {
        log.info("[PAYMENT JOB] Đang quét vé AWAITING_PAYMENT định kỳ...");
        List<Booking> awaitingBookings = bookingService.getExpiredBookingsByStatus(BookingStatus.AWAITING_PAYMENT);
        if (!awaitingBookings.isEmpty()) {
            log.info("[PAYMENT JOB] Đối soát {} vé AWAITING_PAYMENT...", awaitingBookings.size());

            for (Booking booking : awaitingBookings) {
                try {
                    log.info("[PAYMENT JOB] Đối soát vé {}", booking.getPnrCode());
                    PaymentStatus status = paymentService.queryTransaction(booking);
                    if (status == PaymentStatus.FAILED) {
                        // 1. CHẮC CHẮN THẤT BẠI: VNPAY xác nhận lỗi (hết tiền) hoặc không tồn tại mã (91) -> BĂM!
                        log.info("[PAYMENT JOB] Đối soát thất bại. Tiến hành hủy PNR: {}", booking.getPnrCode());
                        bookingService.cancelSingleBookingBySystem(booking.getId());

                    } else if (status == PaymentStatus.SUCCESS) {
                        // 2. CHẮC CHẮN THÀNH CÔNG: Đã lưu Transaction và sinh vé ở PaymentService
                        log.info("[PAYMENT JOB] Vé {} cứu hộ thành công!", booking.getPnrCode());

                    } else {
                        // 3. PENDING (LỖI MẠNG): Không làm gì cả, bỏ qua để 1 phút sau Job quay lại hỏi tiếp!
                        log.warn("[PAYMENT JOB] Lỗi kết nối API VNPAY cho PNR: {}. Tạm bỏ qua, chờ chu kỳ quét tiếp theo.", booking.getPnrCode());
                    }
                } catch (Exception e) {
                    log.error("Lỗi đối soát PNR: {}", booking.getPnrCode(), e);
                }
            }
        }
    }
}