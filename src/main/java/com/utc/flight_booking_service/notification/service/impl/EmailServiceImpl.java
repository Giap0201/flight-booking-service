package com.utc.flight_booking_service.notification.service.impl;

import com.utc.flight_booking_service.notification.dto.NewPasswordEmailRequest;
import com.utc.flight_booking_service.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    JavaMailSender mailSender;
    SpringTemplateEngine templateEngine;

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
