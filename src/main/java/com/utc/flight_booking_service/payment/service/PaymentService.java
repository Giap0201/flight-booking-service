package com.utc.flight_booking_service.payment.service;


import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.payment.config.VNPayConfig;
import com.utc.flight_booking_service.payment.entity.Transaction;
import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import com.utc.flight_booking_service.payment.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final BookingService bookingService;
    private final TransactionRepository transactionRepository;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String secretKey;

    @Value("${vnpay.url}")
    private String vnpPayUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    @Value("${vnpay.api-url}")
    private String vnpApiUrl;

    public String createPaymentUrl(UUID bookingId, HttpServletRequest request) {
        Booking booking = bookingService.getBookingEntityById(bookingId);

        long amount = booking.getTotalAmount().longValue() * 100L;
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        // Cac tham so bat buoc cau vnpay
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", booking.getPnrCode());
        vnp_Params.put("vnp_OrderInfo", "Thanh_toan_ve_PNR_" + booking.getPnrCode());

        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // Xu li dinh dang ngay thang
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        // ep mui gio cho formatter
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // ngay het han (15p)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // 3. Xây dựng chuỗi dữ liệu (Data string) để băm và tạo Query URL
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

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

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(secretKey, hashData.toString());

        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnpPayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    public Map<String, String> processIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            // 1. Gom tham số và xác thực chữ ký (Lớp 1)
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            String signValue = VNPayConfig.hashAllFields(fields, secretKey);
            if (!signValue.equals(vnp_SecureHash)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            String pnrCode = request.getParameter("vnp_TxnRef");
            String vnpAmount = request.getParameter("vnp_Amount");

            // 2. Tìm vé trong DB (Lớp 2)
            Booking booking = bookingService.getBookingEntityByPnr(pnrCode);
            if (booking == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
                return response;
            }

            // 3. Kiểm tra số tiền (Lớp 3)
            long expectedAmount = booking.getTotalAmount().longValue() * 100L;
            if (expectedAmount != Long.parseLong(vnpAmount)) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
                return response;
            }

            // 4. KIỂM TRA IDEMPOTENCY (Tránh trùng lặp)
            // Nếu vé không còn PENDING và không phải AWAITING_PAYMENT, nghĩa là đã xử lý xong từ trước.
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.CANCELLED) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }
            String bankRefNo = request.getParameter("vnp_BankTranNo");
            if (bankRefNo != null && transactionRepository.existsByBankRefNo(bankRefNo)) {
                response.put("RspCode", "02");
                response.put("Message", "Transaction already processed");
                return response;
            }

            String responseCode = request.getParameter("vnp_ResponseCode");
            PaymentStatus currentStatus = "00".equals(responseCode) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

            // 5. LUÔN LƯU TRANSACTION (Phục vụ đối soát, Audit)
            Transaction transaction = Transaction.builder()
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .paymentMethod("VNPAY")
                    .transactionNo(pnrCode)
                    .bankRefNo(request.getParameter("vnp_TransactionNo"))
                    .gatewayResponse(fields.toString())
                    .status(currentStatus)
                    .build();
            transactionRepository.save(transaction);

            // 6. CHỈ CẬP NHẬT BOOKING KHI SUCCESS
            if ("00".equals(responseCode)) {
                bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
                bookingService.issueTicketsForBooking(booking.getId());
            }
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return response;
        }
    }

    public String processReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = VNPayConfig.hashAllFields(fields, secretKey);
        String pnrCode = request.getParameter("vnp_TxnRef");
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                Booking booking = bookingService.getBookingEntityByPnr(pnrCode);
                if (booking != null && booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.CANCELLED) {
                    String bankRefNo = request.getParameter("vnp_BankTranNo");
                    if (bankRefNo != null && !transactionRepository.existsByBankRefNo(bankRefNo)) {
                        Transaction transaction = Transaction.builder()
                                .bookingId(booking.getId())
                                .amount(booking.getTotalAmount())
                                .paymentMethod("VNPAY")
                                .transactionNo(pnrCode)
                                .bankRefNo(bankRefNo)
                                .gatewayResponse(fields.toString())
                                .status(PaymentStatus.SUCCESS)
                                .build();
                        transactionRepository.save(transaction);
                    }
                    bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
                    bookingService.issueTicketsForBooking(booking.getId());
                }
                String queryString = request.getQueryString();
                return "http://localhost:3000?" + queryString;
            } else {
                return "http://localhost:3000/payment-failed?pnr=" + pnrCode;
            }
        } else {
            return "http://localhost:3000/payment-error?message=invalid-signature";
        }
    }

    public PaymentStatus queryTransaction(Booking booking) {
        String vnp_RequestId = UUID.randomUUID().toString();
        String vnp_Version = "2.1.0";
        String vnp_Command = "querydr";
        String vnp_TmnCode = vnpTmnCode;
        String vnp_TxnRef = booking.getPnrCode();
        String vnp_OrderInfo = "Query transaction " + vnp_TxnRef;
        String vnp_IpAddr = "127.0.0.1"; // Chạy ngầm nên truyền IP server

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // Lấy thời gian tạo đơn hàng làm vnp_TransactionDate (phải khớp với lúc tạo URL)
        Date txnDate = Date.from(booking.getCreatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
        String vnp_TransactionDate = formatter.format(txnDate);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());

        // 1. TẠO CHUỖI HASH DATA (Phải chuẩn xác từng dấu gạch đứng | )
        String hashData = String.join("|",
                vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                vnp_TxnRef, vnp_TransactionDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

        String vnp_SecureHash = VNPayConfig.hmacSHA512(secretKey, hashData);

        // 2. ĐÓNG GÓI JSON BODY
        Map<String, String> requestData = new HashMap<>();
        requestData.put("vnp_RequestId", vnp_RequestId);
        requestData.put("vnp_Version", vnp_Version);
        requestData.put("vnp_Command", vnp_Command);
        requestData.put("vnp_TmnCode", vnp_TmnCode);
        requestData.put("vnp_TxnRef", vnp_TxnRef);
        requestData.put("vnp_OrderInfo", vnp_OrderInfo);
        requestData.put("vnp_TransactionDate", vnp_TransactionDate);
        requestData.put("vnp_CreateDate", vnp_CreateDate);
        requestData.put("vnp_IpAddr", vnp_IpAddr);
        requestData.put("vnp_SecureHash", vnp_SecureHash);

        // 3. GỌI HTTP POST SANG VNPAY
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(vnpApiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String responseCode = (String) responseBody.get("vnp_ResponseCode");
                String transactionStatus = (String) responseBody.get("vnp_TransactionStatus");

                // Nếu VNPAY báo: Request hợp lệ (00) VÀ Khách đã trả tiền thành công (00)
                if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                    return PaymentStatus.SUCCESS;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API Đối soát VNPAY cho PNR: " + vnp_TxnRef);
        }

        return PaymentStatus.PENDING; // Trả về PENDING để lần sau Job quét tiếp
    }

}