package com.utc.flight_booking_service.payment.service;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.entity.BookingFlight;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.enums.TicketStatus;
import com.utc.flight_booking_service.booking.repository.BookingRepository;
import com.utc.flight_booking_service.booking.service.BookingService;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.inventory.service.FlightClassService;
import com.utc.flight_booking_service.inventory.service.IFlightClassService;
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

import java.math.BigDecimal;
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

    // ⚡ FIX 1: Đưa reactBaseUrl ra biến môi trường để không bị hardcode khi deploy
    @Value("${frontend.url:http://localhost:5173}")
    private String reactBaseUrl;

    public String createPaymentUrl(UUID bookingId, String platform, HttpServletRequest request) {
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

        String finalReturnUrl = vnpReturnUrl;
        if (platform != null && !platform.trim().isEmpty()) {
            finalReturnUrl = finalReturnUrl + (finalReturnUrl.contains("?") ? "&" : "?") + "platform=" + platform;
        }
        vnp_Params.put("vnp_ReturnUrl", finalReturnUrl);

        // ⚡ FIX 2: Truyền đúng IP của khách hàng thay vì hardcode 127.0.0.1
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

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

            Booking booking = bookingService.getBookingEntityByPnr(pnrCode);
            if (booking == null) {
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
                return response;
            }

            long expectedAmount = booking.getTotalAmount().longValue() * 100L;
            if (expectedAmount != Long.parseLong(vnpAmount)) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
                return response;
            }

            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.CANCELLED) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            String bankRefNo = request.getParameter("vnp_BankTranNo");
            String transactionNo = request.getParameter("vnp_TransactionNo");

            if (bankRefNo == null || bankRefNo.trim().isEmpty()) {
                bankRefNo = "CANCEL_" + UUID.randomUUID().toString().substring(0, 8);
            }
            if (transactionNo == null || transactionNo.trim().isEmpty() || "0".equals(transactionNo)) {
                transactionNo = "CANCEL_" + UUID.randomUUID().toString().substring(0, 8);
            }

            if (!bankRefNo.startsWith("CANCEL_") && transactionRepository.existsByBankRefNo(bankRefNo)) {
                response.put("RspCode", "02");
                response.put("Message", "Transaction already processed");
                return response;
            }

            String responseCode = request.getParameter("vnp_ResponseCode");
            PaymentStatus currentStatus = "00".equals(responseCode) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

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

            if ("00".equals(responseCode)) {
//                bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
                bookingService.issueTicketsForBooking(booking.getId());
                emailService.sendBookingConfirmationEmail(bookingService.getBookingById(booking.getId()));
                log.info("Gửi mail thành công cho PNR: {}", booking.getPnrCode());
            } else {
                log.warn("Thanh toán thất bại/Khách hủy giao dịch cho PNR: {}", booking.getPnrCode());
            }

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;

        } catch (Exception e) {
            log.error("Lỗi IPN: ", e);
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

            if ((fieldValue != null) && (fieldValue.length() > 0) && fieldName.startsWith("vnp_")) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = VNPayConfig.hashAllFields(fields, secretKey);

        String pnrCode = request.getParameter("vnp_TxnRef");
        String platform = request.getParameter("platform");
        String responseCode = request.getParameter("vnp_ResponseCode");

        Booking booking = bookingService.getBookingEntityByPnr(pnrCode);

        // ⚡ FIX 3: Sử dụng biến reactBaseUrl đã được khai báo @Value ở đầu class
        if (signValue.equals(vnp_SecureHash)) {
            if ("android".equals(platform)) {
                return "flightbooking://payment-result?code=" + responseCode +
                        "&pnrCode=" + pnrCode +
                        "&bookingId=" + (booking != null ? booking.getId() : "");
            }

            if ("00".equals(responseCode)) {
                return reactBaseUrl + "/payment-success?" + request.getQueryString();
            } else {
                return reactBaseUrl + "/payment-failed?pnr=" + pnrCode;
            }
        } else {
            log.error("Sai chữ ký VNPAY Return cho PNR: {}", pnrCode);
            if ("android".equals(platform)) {
                return "flightbooking://payment-result?code=99&pnrCode=" + pnrCode;
            }
            return reactBaseUrl + "/payment-error?message=invalid-signature";
        }
    }

    public PaymentStatus queryTransaction(Booking booking) {
        String vnp_RequestId = UUID.randomUUID().toString();
        String vnp_Version = "2.1.0";
        String vnp_Command = "querydr";
        String vnp_TmnCode = vnpTmnCode;
        String vnp_TxnRef = booking.getPnrCode();
        String vnp_OrderInfo = "Query transaction " + vnp_TxnRef;

        // ⚡ FIX 4 (Tùy chọn): Đối với gọi API ngầm (QueryDR), VNPay thường bỏ qua check IP,
        // nhưng để chuẩn nhất thì dùng IP hiện tại của server (nếu lấy được)
        // Ở đây tạm giữ 127.0.0.1 vì API đối soát ít bị bắt bẻ IP hơn là API tạo link thanh toán.
        String vnp_IpAddr = "127.0.0.1";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        Date txnDate = Date.from(booking.getCreatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
        String vnp_TransactionDate = formatter.format(txnDate);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());

        String hashData = String.join("|",
                vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                vnp_TxnRef, vnp_TransactionDate, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

        String vnp_SecureHash = VNPayConfig.hmacSHA512(secretKey, hashData);

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
                                        .transactionNo(vnpTransactionNo)
                                        .bankRefNo(vnpBankTranNo)
                                        .gatewayResponse(responseBody.toString())
                                        .status(PaymentStatus.SUCCESS)
                                        .build();
                                transactionRepository.save(transaction);
                            }

//                            bookingService.updateBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
                            bookingService.issueTicketsForBooking(booking.getId());
                            emailService.sendBookingConfirmationEmail(bookingService.getBookingById(booking.getId()));
                            log.info("JOB ĐỐI SOÁT ĐÃ CỨU HỘ THÀNH CÔNG VÉ: {}", booking.getPnrCode());
                        }
                        return PaymentStatus.SUCCESS;
                    } else {
                        log.warn("VNPAY báo giao dịch thất bại cho PNR {}. Mã lỗi: {}", booking.getPnrCode(), transactionStatus);

                        if (vnpBankTranNo == null || vnpBankTranNo.isEmpty()) {
                            vnpBankTranNo = "CANCEL_" + UUID.randomUUID().toString().substring(0, 8);
                        }
                        if (vnpTransactionNo == null || vnpTransactionNo.isEmpty() || "0".equals(vnpTransactionNo)) {
                            vnpTransactionNo = "CANCEL_" + UUID.randomUUID().toString().substring(0, 8);
                        }

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
                else if ("91".equals(responseCode)) {
                    log.info("Khách hàng chưa từng quét mã/nhập thẻ cho PNR: {}", booking.getPnrCode());

                    Transaction transaction = Transaction.builder()
                            .bookingId(booking.getId())
                            .amount(booking.getTotalAmount())
                            .paymentMethod("VNPAY")
                            .transactionNo("CANCEL_" + UUID.randomUUID().toString().substring(0, 8))
                            .bankRefNo("CANCEL_" + UUID.randomUUID().toString().substring(0, 8))
                            .gatewayResponse(responseBody.toString())
                            .status(PaymentStatus.FAILED)
                            .build();
                    transactionRepository.save(transaction);

                    return PaymentStatus.FAILED;
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi API Đối soát VNPAY cho PNR: {}", booking.getPnrCode(), e);
        }

        return PaymentStatus.PENDING;
    }


    public Map<String, String> verifyPaymentStatusImmediately(String pnrCode) {
        Booking booking = bookingService.getBookingEntityByPnr(pnrCode);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return Map.of("status", "SUCCESS", "message", "Thanh toán thành công");
        }

        if (booking.getStatus() == BookingStatus.AWAITING_PAYMENT) {
            PaymentStatus actualStatus = queryTransaction(booking);

            if (actualStatus == PaymentStatus.SUCCESS) {
                return Map.of("status", "SUCCESS", "message", "Cứu hộ thành công! Vé đã được xuất.");
            } else if (actualStatus == PaymentStatus.FAILED) {
                return Map.of("status", "FAILED", "message", "Giao dịch thất bại tại VNPAY. Vui lòng thử lại.");
            }
        }

        return Map.of("status", booking.getStatus().name(), "message", "Trạng thái hiện tại của vé: " + booking.getStatus().name());
    }




}