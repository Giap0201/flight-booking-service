package com.utc.flight_booking_service.notification.service.impl;

import com.utc.flight_booking_service.booking.response.client.BookingDetailResponse;
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

    public void sendBookingConfirmationEmail(BookingDetailResponse bookingData) {
        try {
            log.info("Đang tiến hành gửi email xác nhận cho PNR: {}", bookingData.getPnrCode());


            Context context = new Context();
            context.setVariable("booking", bookingData);


            String htmlContent = templateEngine.process("email/booking-confirmation", context);


            MimeMessage message = mailSender.createMimeMessage();


            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");


            helper.setTo(bookingData.getContact().getEmail());
            helper.setSubject("✈️ Xác nhận đặt chỗ thành công - Mã PNR: " + bookingData.getPnrCode());
            helper.setText(htmlContent, true);


            mailSender.send(message);

            log.info("✅ Gửi email thành công tới: {}", bookingData.getContact().getEmail());

        } catch (MessagingException e) {
            log.error("❌ Lỗi khi gửi email cho PNR {}: {}", bookingData.getPnrCode(), e.getMessage());

        }
    }

    @Async("taskExecutor")
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
