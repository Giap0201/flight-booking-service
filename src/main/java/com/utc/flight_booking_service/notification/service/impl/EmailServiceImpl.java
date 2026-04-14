package com.utc.flight_booking_service.notification.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.utc.flight_booking_service.booking.response.client.BookingDetailResponse;
import com.utc.flight_booking_service.notification.dto.NewPasswordEmailRequest;
import com.utc.flight_booking_service.notification.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    SpringTemplateEngine templateEngine;
    @NonFinal
    @Value("${spring.sendgrid.api-key}")
    String sendGridApiKey;

    // @Async("taskExecutor")

    public void sendBookingConfirmationEmail(BookingDetailResponse bookingData) {
        try {
            log.info("Đang tiến hành gửi email xác nhận cho PNR: {}", bookingData.getPnrCode());


            Context context = new Context();
            context.setVariable("booking", bookingData);
            String htmlContent = templateEngine.process("email/booking-confirmation", context);


            Email from = new Email("nghianickmoi4@gmail.com");

            String subject = "✈️ Xác nhận đặt chỗ thành công - Mã PNR: " + bookingData.getPnrCode();
            Email to = new Email(bookingData.getContact().getEmail());
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);


            SendGrid sg = new SendGrid(sendGridApiKey);
            Request sgRequest = new Request();

            sgRequest.setMethod(Method.POST);
            sgRequest.setEndpoint("mail/send");
            sgRequest.setBody(mail.build());

            Response response = sg.api(sgRequest);

            log.info("Trạng thái phản hồi SendGrid: {}", response.getStatusCode());
            log.info("✅ Gửi email thành công tới: {}", bookingData.getContact().getEmail());

        } catch (IOException e) {
            // ĐÃ SỬA: SendGrid ném ra IOException chứ không phải MessagingException
            log.error("❌ Lỗi khi gửi email cho PNR {}: {}", bookingData.getPnrCode(), e.getMessage());
            throw new RuntimeException("Cannot send booking confirmation email via SendGrid", e);
        }
    }

    //@Async("taskExecutor")
    @Override
    public void sendNewPasswordEmail(NewPasswordEmailRequest request) {
        try {
            // 1. Dùng Thymeleaf tạo HTML như cũ
            Context context = new Context();
            context.setVariable("name", request.getName());
            context.setVariable("newPassword", request.getNewPassword());
            String htmlContent = templateEngine.process("email/new-password", context);

            Email from = new Email("nghianickmoi4@gmail.com");
            String subject = "Mật khẩu mới của bạn từ StingAir";
            Email to = new Email(request.getTo());
            Content content = new Content("text/html", htmlContent);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request sgRequest = new Request();

            sgRequest.setMethod(Method.POST);
            sgRequest.setEndpoint("mail/send");
            sgRequest.setBody(mail.build());

            Response response = sg.api(sgRequest);
            log.info("Trạng thái gửi mail SendGrid: {}", response.getStatusCode());

        } catch (IOException e) {
            log.error("Lỗi khi gọi SendGrid API: ", e);
            // Ném lỗi ra để Controller bắt và rollback Database (nếu có)
            throw new RuntimeException("Cannot send email via SendGrid", e);
        }
    }
}
