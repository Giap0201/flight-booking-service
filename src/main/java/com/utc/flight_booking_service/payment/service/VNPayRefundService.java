package com.utc.flight_booking_service.payment.service;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.payment.config.VNPayConfig;
import com.utc.flight_booking_service.payment.entity.Transaction;
import com.utc.flight_booking_service.payment.enums.PaymentStatus;
import com.utc.flight_booking_service.payment.repository.TransactionRepository;
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

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2

public class VNPayRefundService {
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

    @Value("${frontend.url:http://localhost:5173}")
    private String reactBaseUrl;

    @Transactional
    public void processRealRefund(Booking booking, Transaction originalTransaction) {
        log.info("Bắt đầu gọi API VNPay yêu cầu hoàn tiền cho PNR: {}", booking.getPnrCode());

        String vnp_RequestId = UUID.randomUUID().toString().replace("-", "");
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TmnCode = vnpTmnCode;
        String vnp_TransactionType = "02"; // 02: Hoàn trả toàn phần
        String vnp_TxnRef = booking.getPnrCode();
        long amount = booking.getTotalAmount().longValue() * 100L;
        String vnp_Amount = String.valueOf(amount);
        String vnp_OrderInfo = "Hoan tien ve may bay PNR " + booking.getPnrCode();

        // TransactionNo của VNPay trả về lúc thanh toán thành công (nếu có, nếu không để trống)
        String vnp_TransactionNo = originalTransaction.getTransactionNo() != null ? originalTransaction.getTransactionNo() : "";

        String vnp_CreateBy = "Admin"; // Người thực hiện lệnh hoàn
        String vnp_IpAddr = "127.0.0.1"; // IP Server gọi API

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // Thời gian tạo request hoàn tiền (hiện tại)
        String vnp_CreateDate = formatter.format(new Date());

        // Thời gian ghi nhận giao dịch gốc (lấy từ created_at của originalTransaction)
        Date txnDate = Date.from(originalTransaction.getCreatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
        String vnp_TransactionDate = formatter.format(txnDate);

        // Tạo chuỗi checksum theo đúng chuẩn Document bạn gửi
        String hashData = String.join("|",
                vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo,
                vnp_TransactionDate, vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

        String vnp_SecureHash = VNPayConfig.hmacSHA512(secretKey, hashData);

        // Build Body Request (JSON)
        Map<String, String> requestData = new HashMap<>();
        requestData.put("vnp_RequestId", vnp_RequestId);
        requestData.put("vnp_Version", vnp_Version);
        requestData.put("vnp_Command", vnp_Command);
        requestData.put("vnp_TmnCode", vnp_TmnCode);
        requestData.put("vnp_TransactionType", vnp_TransactionType);
        requestData.put("vnp_TxnRef", vnp_TxnRef);
        requestData.put("vnp_Amount", vnp_Amount);
        requestData.put("vnp_TransactionNo", vnp_TransactionNo);
        requestData.put("vnp_TransactionDate", vnp_TransactionDate);
        requestData.put("vnp_CreateBy", vnp_CreateBy);
        requestData.put("vnp_CreateDate", vnp_CreateDate);
        requestData.put("vnp_IpAddr", vnp_IpAddr);
        requestData.put("vnp_OrderInfo", vnp_OrderInfo);
        requestData.put("vnp_SecureHash", vnp_SecureHash);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestData, headers);

        try {
            // Gọi API sang VNPay (Dùng chung URL với hàm querydr)
            ResponseEntity<Map> response = restTemplate.postForEntity(vnpApiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String responseCode = (String) responseBody.get("vnp_ResponseCode");
                String message = (String) responseBody.get("vnp_Message");
                String vnpBankTranNo = (String) responseBody.get("vnp_BankCode");
                String vnpTxnNoResp = (String) responseBody.get("vnp_TransactionNo");

                log.info("Kết quả gọi VNPay Refund: Code = {}, Message = {}", responseCode, message);

                PaymentStatus refundStatus = "00".equals(responseCode) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

                // Ghi nhận kết quả vào Database
                Transaction refundTxn = Transaction.builder()
                        .bookingId(booking.getId())
                        .amount(booking.getTotalAmount())
                        .paymentMethod("VNPAY_REFUND")
                        .transactionNo(vnpTxnNoResp != null ? vnpTxnNoResp : "REFUND_" + System.currentTimeMillis())
                        .bankRefNo(vnpBankTranNo != null ? vnpBankTranNo : "N/A")
                        .gatewayResponse(responseBody.toString())
                        .status(refundStatus)
                        .build();
                transactionRepository.save(refundTxn);
            }
        } catch (Exception e) {
            log.error("Lỗi Exception khi gọi API Hoàn tiền VNPay cho PNR: {}", booking.getPnrCode(), e);
            Transaction failTxn = Transaction.builder()
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .paymentMethod("VNPAY_REFUND")
                    .gatewayResponse("Lỗi mạng/Exception: " + e.getMessage())
                    .status(PaymentStatus.FAILED)
                    .build();
            transactionRepository.save(failTxn);
        }
    }


}
