package com.utc.flight_booking_service.payment.service;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.notification.service.EmailService;
import com.utc.flight_booking_service.payment.config.VNPayConfig;
import com.utc.flight_booking_service.payment.entity.Transaction;
import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import com.utc.flight_booking_service.payment.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class PaymentService {

    private final BookingService bookingService;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;


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


        // Build chuỗi dữ liệu
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

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
        bookingService.updateBookingStatus(booking.getId(), BookingStatus.AWAITING_PAYMENT);
        return vnpPayUrl + "?" + queryUrl;
    }

    public Map<String, String> processIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            // 1. Gom tham số
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

            // 2. Kiểm tra DB
            Booking booking = bookingService.getBookingEntityByPnr(pnrCode);
            if (booking == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
                return response;
            }

            // 3. Kiểm tra số tiền
            long expectedAmount = booking.getTotalAmount().longValue() * 100L;
            if (expectedAmount != Long.parseLong(vnpAmount)) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
                return response;
            }

            // 4. Kiểm tra Idempotency
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.CANCELLED) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            String bankRefNo = request.getParameter("vnp_BankTranNo");
            String transactionNo = request.getParameter("vnp_TransactionNo"); // FIXED: Tách riêng mã giao dịch VNPAY

            if (bankRefNo != null && transactionRepository.existsByBankRefNo(bankRefNo)) {
                response.put("RspCode", "02");
                response.put("Message", "Transaction already processed");
                return response;
            }

            String responseCode = request.getParameter("vnp_ResponseCode");
            PaymentStatus currentStatus = "00".equals(responseCode) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

            // 5. Lưu Transaction
            Transaction transaction = Transaction.builder()
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .paymentMethod("VNPAY")
                    .transactionNo(transactionNo)
                    .bankRefNo(bankRefNo)
                    .gatewayResponse(fields.toString())
                    .status(currentStatus)
                    .build();
            transactionRepository.save(transaction);

            // 6. Cập nhật trạng thái Booking
            if ("00".equals(responseCode)) {
                bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
                bookingService.issueTicketsForBooking(booking.getId());
                emailService.sendBookingConfirmationEmail(bookingService.getBookingById(booking.getId()));
                log.info("gui mail thanh cong");
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

    // FIXED: Hàm Return chỉ còn nhiệm vụ đá trang (Redirect), tuyệt đối không chọc DB
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
                // Thành công -> Đá về ReactJS kèm query string (để FE tự vẽ bill nếu cần)
                return "http://localhost:5173/payment-success?" + request.getQueryString();
            } else {
                // Thất bại
                return "http://localhost:5173/payment-failed?pnr=" + pnrCode;
            }
        } else {
            // Sai chữ ký (Có nguy cơ bị hacker chọc phá)
            return "http://localhost:5173/payment-error?message=invalid-signature";
        }
    }

    public PaymentStatus queryTransaction(Booking booking) {
        String vnp_RequestId = UUID.randomUUID().toString();
        String vnp_Version = "2.1.0";
        String vnp_Command = "querydr";
        String vnp_TmnCode = vnpTmnCode;
        String vnp_TxnRef = booking.getPnrCode();
        String vnp_OrderInfo = "Query transaction " + vnp_TxnRef;
        String vnp_IpAddr = "127.0.0.1";

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
                if ("00".equals(responseCode)) {
                    String vnpTransactionNo = (String) responseBody.get("vnp_TransactionNo");
                    String vnpBankTranNo = (String) responseBody.get("vnp_BankTranNo");

                    if ("00".equals(transactionStatus)) {
                        if (booking.getStatus() != BookingStatus.CONFIRMED) {

                            if (vnpTransactionNo != null && !transactionRepository.existsByTransactionNo(vnpTransactionNo)) {
                                Transaction transaction = Transaction.builder()
                                        .bookingId(booking.getId())
                                        .amount(booking.getTotalAmount())
                                        .paymentMethod("VNPAY")
                                        .transactionNo(vnpTransactionNo) // Mã này 100% có
                                        .bankRefNo(vnpBankTranNo)
                                        .gatewayResponse(responseBody.toString())
                                        .status(PaymentStatus.SUCCESS)
                                        .build();
                                transactionRepository.save(transaction);
                            }

                            bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
                            bookingService.issueTicketsForBooking(booking.getId());
                            emailService.sendBookingConfirmationEmail(bookingService.getBookingById(booking.getId()));
                            log.info("JOB ĐỐI SOÁT ĐÃ CỨU HỘ THÀNH CÔNG VÉ: {}", booking.getPnrCode());

                        }
                        return PaymentStatus.SUCCESS;
                    } else {
                        log.warn("VNPAY báo giao dịch thất bại cho PNR {}. Mã lỗi: {}", booking.getPnrCode(), transactionStatus);
                        Transaction transaction = Transaction.builder()
                                .bookingId(booking.getId())
                                .amount(booking.getTotalAmount())
                                .paymentMethod("VNPAY")
                                .transactionNo(vnpTransactionNo)
                                .bankRefNo(vnpBankTranNo)
                                .gatewayResponse(responseBody.toString())
                                .status(PaymentStatus.FAILED)
                                .build();
                        transactionRepository.save(transaction);

                        return PaymentStatus.FAILED;
                    }
                }
                // 2. NẾU VNPAY BÁO KHÔNG TÌM THẤY GIAO DỊCH (Mã 91)
                else if ("91".equals(responseCode)) {
                    log.info("Khách hàng chưa từng quét mã/nhập thẻ cho PNR: {}", booking.getPnrCode());
                    return PaymentStatus.FAILED;
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi API Đối soát VNPAY cho PNR: {}", booking.getPnrCode(), e);
        }

        // Nếu gọi API có lỗi mạng, trả về PENDING để lần quét Job sau (1 phút nữa) gọi lại
        return PaymentStatus.PENDING;
    }

    public Map<String, String> verifyPaymentStatusImmediately(String pnrCode) {
        Booking booking = bookingService.getBookingEntityByPnr(pnrCode);
        // 1. Nếu IPN đã chạy mượt mà trước đó rồi -> Trả về luôn
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return Map.of("status", "SUCCESS", "message", "Thanh toán thành công");
        }

        // 2. Nếu vẫn thấy AWAITING_PAYMENT (IPN rớt mạng) -> ÉP GỌI VNPAY NGAY LẬP TỨC!
        if (booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
            PaymentStatus actualStatus = queryTransaction(booking);

            if (actualStatus == PaymentStatus.SUCCESS) {
                return Map.of("status", "SUCCESS", "message", "Cứu hộ thành công! Vé đã được xuất.");
            } else if (actualStatus == PaymentStatus.FAILED) {
                return Map.of("status", "FAILED", "message", "Giao dịch thất bại tại VNPAY. Vui lòng thử lại.");
            }
        }

        // 3. Các trạng thái khác (PENDING, CANCELLED...)
        return Map.of("status", booking.getStatus().name(), "message", "Trạng thái hiện tại của vé: " + booking.getStatus().name());
    }
}