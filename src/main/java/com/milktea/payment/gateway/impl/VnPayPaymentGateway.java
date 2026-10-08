package com.milktea.payment.gateway.impl;

import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.VnPayProperties;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VnPayPaymentGateway implements PaymentGateway {
    private final VnPayProperties vnPayProperties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Override
    public String getMethod() {
        return "VNPAY";
    }

    @Override
    public GatewayPaymentResult processPayment(GatewayPaymentRequest request) {
        String txnRef = request.transactionReference() != null && !request.transactionReference().isBlank()
                ? request.transactionReference()
                : request.orderId() + "_" + System.currentTimeMillis();

        long amount = request.amount().multiply(BigDecimal.valueOf(100)).longValue();
        Instant now = Instant.now();
        String createDate = DATE_FORMATTER.format(now);
        String expireDate = DATE_FORMATTER.format(now.plus(15, ChronoUnit.MINUTES));

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", request.description() != null && !request.description().isBlank()
                ? request.description()
                : "Thanh toan don hang #" + request.orderId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Lỗi mã hóa URL thanh toán VNPay", ex);
        }

        String secureHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        String paymentUrl = vnPayProperties.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;

        return GatewayPaymentResult.builder()
                .success(true)
                .message("Tạo URL thanh toán VNPay thành công")
                .transactionReference(txnRef)
                .redirectUrl(paymentUrl)
                .metadata(Map.of("vnp_TxnRef", txnRef))
                .build();
    }

    @Override
    public GatewayPaymentResult handleCallback(Map<String, String> callbackParams) {
        String vnpSecureHash = callbackParams.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isBlank()) {
            return GatewayPaymentResult.builder()
                    .success(false)
                    .message("Thiếu chữ ký vnp_SecureHash trong kết quả VNPay")
                    .metadata(callbackParams)
                    .build();
        }

        List<String> fieldNames = new ArrayList<>(callbackParams.keySet());
        fieldNames.remove("vnp_SecureHash");
        fieldNames.remove("vnp_SecureHashType");
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = callbackParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Lỗi mã hóa dữ liệu đối soát VNPay", ex);
        }

        String calculatedHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
            return GatewayPaymentResult.builder()
                    .success(false)
                    .message("Chữ ký VNPay không hợp lệ (sai checksum)")
                    .transactionReference(callbackParams.get("vnp_TxnRef"))
                    .metadata(callbackParams)
                    .build();
        }

        String responseCode = callbackParams.get("vnp_ResponseCode");
        boolean isSuccess = "00".equals(responseCode);

        return GatewayPaymentResult.builder()
                .success(isSuccess)
                .message(isSuccess ? "Thanh toán VNPay thành công" : "Thanh toán VNPay không thành công, mã lỗi: " + responseCode)
                .transactionReference(callbackParams.get("vnp_TxnRef"))
                .metadata(callbackParams)
                .build();
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Lỗi tính chữ ký HMAC-SHA512", ex);
        }
    }
}
