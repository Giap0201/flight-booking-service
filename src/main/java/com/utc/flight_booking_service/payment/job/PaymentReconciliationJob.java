package com.utc.flight_booking_service.payment.job;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.repository.BookingRepository;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.payment.entity.Transaction;
import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import com.utc.flight_booking_service.payment.repository.TransactionRepository;
import com.utc.flight_booking_service.payment.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentReconciliationJob {

    BookingRepository bookingRepository;
    PaymentService paymentService;
    BookingService bookingService;
    TransactionRepository transactionRepository;

    // Chạy ngầm định kỳ 5 phút (300,000 milliseconds) một lần
    @Scheduled(fixedDelay = 120)
    public void reconcilePendingPayments() {
        log.info("--- BẮT ĐẦU CHẠY JOB ĐỐI SOÁT VNPAY ---");

        // 1. Tìm các Booking PENDING nhưng phải QUÁ 15 PHÚT (Để tránh giành việc với IPN đang chạy)
        LocalDateTime fifteenMinsAgo = LocalDateTime.now().minusMinutes(15);

        // BẠN CẦN VIẾT THÊM HÀM NÀY TRONG BookingRepository CỦA BẠN:
        // List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime time);
        List<Booking> pendingBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, fifteenMinsAgo);

        for (Booking booking : pendingBookings) {
            log.info("Đang kiểm tra chéo Booking PNR: {}", booking.getPnrCode());

            try {
                // 2. Gọi sang VNPAY hỏi thăm
                PaymentStatus status = paymentService.queryTransaction(booking);

                // 3. Xử lý kết quả
                if (status == PaymentStatus.SUCCESS) {
                    log.info("Phát hiện đơn {} đã trả tiền nhưng kẹt! Đang tiến hành xuất vé...", booking.getPnrCode());

                    // Lưu Transaction vớt vát
                    Transaction transaction = Transaction.builder()
                            .bookingId(booking.getId())
                            .amount(booking.getTotalAmount())
                            .paymentMethod("VNPAY")
                            .transactionNo(booking.getPnrCode())
                            .status(PaymentStatus.SUCCESS)
                            .gatewayResponse("Saved by Cron Job Reconciliation")
                            .build();
                    transactionRepository.save(transaction);

                    // Ép trạng thái và xuất vé
                    bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
                    bookingService.issueTicketsForBooking(booking.getId());
                } else {
                    // Nếu quá 2 tiếng mà VNPAY báo vẫn chưa trả tiền -> Khách bom vé -> HỦY LUÔN
                    LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
                    if (booking.getCreatedAt().isBefore(twoHoursAgo)) {
                        log.info("Đơn {} quá hạn 2 tiếng chưa thanh toán. HỦY ĐƠN!", booking.getPnrCode());
                        bookingService.updateBookingStatus(booking.getId(), BookingStatus.CANCELLED);
                    }
                }
            } catch (Exception e) {
                log.error("Lỗi Job Đối soát cho Booking: " + booking.getPnrCode(), e);
            }
        }
        log.info("--- KẾT THÚC JOB ĐỐI SOÁT ---");
    }
}