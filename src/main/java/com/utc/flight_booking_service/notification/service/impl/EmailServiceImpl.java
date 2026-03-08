package com.utc.flight_booking_service.notification.service.impl;

import com.utc.flight_booking_service.notification.dto.BookingEmailResponse;
import com.utc.flight_booking_service.notification.dto.NewPasswordEmailRequest;
import com.utc.flight_booking_service.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    JavaMailSender mailSender;
    SpringTemplateEngine templateEngine;

    @Async("taskExecutor")
    // CỰC KỲ QUAN TRỌNG: Giúp hàm này chạy ngầm, không làm giật lag web của khách
    public void sendBookingConfirmationEmail(BookingEmailResponse bookingData) {
        try {
            log.info("Đang tiến hành gửi email xác nhận cho PNR: {}", bookingData.getPnrCode());


            // 1. Tạo Context của Thymeleaf và nhồi cục DTO vào biến "booking"
            // (Chữ "booking" này phải khớp 100% với th:text="${booking.pnrCode}" trong file HTML)
            Context context = new Context();
            context.setVariable("booking", bookingData);

            // 2. Render file "booking-confirmation.html" thành chuỗi String HTML
            String htmlContent = templateEngine.process("booking-confirmation", context);

            // 3. Khởi tạo MimeMessage (Dùng cái này mới gửi được HTML và tiếng Việt có dấu)
            MimeMessage message = mailSender.createMimeMessage();

            // true = multipart (hỗ trợ đính kèm file sau này), UTF-8 = chống lỗi font tiếng Việt
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 4. Cấu hình các thông số thư
            helper.setTo(bookingData.getContactEmail()); // Gửi tới email người đặt
            helper.setSubject("✈️ Xác nhận đặt chỗ thành công - Mã PNR: " + bookingData.getPnrCode());
            helper.setText(htmlContent, true); // Chữ 'true' ở đây báo cho Gmail biết đây là code HTML

            // 5. Bấm nút gửi
            mailSender.send(message);

            log.info("✅ Gửi email thành công tới: {}", bookingData.getContactEmail());

        } catch (MessagingException e) {
            log.error("❌ Lỗi khi gửi email cho PNR {}: {}", bookingData.getPnrCode(), e.getMessage());

        }
    }

    @Override
    public void sendNewPasswordEmail(NewPasswordEmailRequest request) {
        try {
            Context context = new Context();
            context.setVariable("name", request.getName());
            context.setVariable("newPassword", request.getNewPassword());

            String html = templateEngine.process(
                    "email/new-password",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(request.getTo());
            helper.setSubject("Mật khẩu mới của bạn");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Cannot send email", e);
        }
    }
}
