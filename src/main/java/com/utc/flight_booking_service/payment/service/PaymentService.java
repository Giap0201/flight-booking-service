package com.utc.flight_booking_service.payment.service;


import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.BookingRepository;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.payment.config.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String secretKey;

    @Value("${vnpay.url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    public String createPaymentUrl(UUID bookingId, HttpServletRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        long amount = booking.getTotalAmount().longValue() * 100L;

        // 2. Khởi tạo các tham số bắt buộc của VNPAY
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0"); // Cố định
        vnp_Params.put("vnp_Command", "pay"); // Cố định
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND"); // Tiền Việt Nam

        // ĐÃ XÓA DÒNG BANKCODE để VNPAY tự hiện danh sách ngân hàng

        // SỬA 1: Dùng PNR Code làm mã giao dịch cho an toàn
        vnp_Params.put("vnp_TxnRef", booking.getPnrCode());

        // SỬA 2: Xóa dấu cách, dùng dấu gạch dưới
        vnp_Params.put("vnp_OrderInfo", "Thanh_toan_ve_PNR_" + booking.getPnrCode());

        vnp_Params.put("vnp_OrderType", "other"); // Loại hàng hóa
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // Xử lý định dạng ngày tháng
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        // ---> THÊM ĐÚNG DÒNG NÀY VÀO ĐỂ ÉP MÚI GIỜ CHO THẰNG FORMATTER <---
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Ngày hết hạn (15 phút sau)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // 3. Xây dựng chuỗi dữ liệu (Data string) để băm và tạo Query URL
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames); // VNPAY yêu cầu phải sort Key theo A-Z

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                // SỬA 3: Đổi toàn bộ thành UTF_8
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 4. Mã hóa chuỗi Data và thêm chữ ký (vnp_SecureHash) vào cuối URL
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(secretKey, hashData.toString());

        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        // 5. Trả về URL hoàn chỉnh
        String paymentUrl = vnpPayUrl + "?" + queryUrl;
        return paymentUrl;
    }
}