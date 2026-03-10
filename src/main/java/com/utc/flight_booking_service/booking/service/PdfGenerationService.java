package com.utc.flight_booking_service.booking.service;

import com.lowagie.text.pdf.BaseFont;
import com.utc.flight_booking_service.booking.response.share.ETicketEmailModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final TemplateEngine templateEngine;

    public byte[] generateTicketPdf(List<ETicketEmailModel> tickets) {

        Context context = new Context();
        context.setVariable("tickets", tickets);

        String htmlContent = templateEngine.process("ticket", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            // Lấy đường dẫn tới file arial.ttf trong thư mục resources/fonts/
            String fontPath = getClass().getResource("/fonts/arial.ttf").toString();

            // Ép thư viện sử dụng font này, hỗ trợ Unicode (IDENTITY_H) và nhúng thẳng vào PDF (EMBEDDED)
            renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Lỗi khi tạo file PDF", e);
            throw new RuntimeException("Không thể tạo vé PDF", e);
        }
    }
}