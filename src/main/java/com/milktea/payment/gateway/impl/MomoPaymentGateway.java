package com.milktea.payment.gateway.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.common.exception.BusinessException;
import com.milktea.payment.gateway.MomoProperties;
import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import com.milktea.payment.gateway.dto.MomoCreatePaymentRequest;
import com.milktea.payment.gateway.dto.MomoCreatePaymentResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MomoPaymentGateway implements PaymentGateway {
    private final MomoProperties momoProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public MomoPaymentGateway(MomoProperties momoProperties, ObjectMapper objectMapper) {
        this(momoProperties, objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public MomoPaymentGateway(MomoProperties momoProperties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.momoProperties = momoProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public String getMethod() {
        return "MOMO";
    }

    @Override
    public GatewayPaymentResult processPayment(GatewayPaymentRequest request) {
        String orderId = request.transactionReference() != null && !request.transactionReference().isBlank()
                ? request.transactionReference()
                : request.orderId() + "_" + System.currentTimeMillis();

        String requestId = orderId + "_" + System.currentTimeMillis();
        long amount = request.amount().longValue();
        String orderInfo = request.description() != null && !request.description().isBlank()
                ? request.description()
                : "Thanh toan don hang #" + request.orderId();

        String requestType = "captureWallet";
        String extraData = "";

        // 1. Tạo chuỗi ký tự rawSignature theo thứ tự chuẩn MoMo v3 Collection Link:
        String rawSignature = "accessKey=" + momoProperties.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + momoProperties.getIpnUrl()
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoProperties.getPartnerCode()
                + "&redirectUrl=" + momoProperties.getReturnUrl()
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        // 2. Ký HMAC-SHA256 bằng secretKey
        String signature = hmacSHA256(momoProperties.getSecretKey(), rawSignature);

        // 3. Chuẩn bị request payload
        MomoCreatePaymentRequest momoRequest = MomoCreatePaymentRequest.builder()
                .partnerCode(momoProperties.getPartnerCode())
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(momoProperties.getReturnUrl())
                .ipnUrl(momoProperties.getIpnUrl())
                .requestType(requestType)
                .extraData(extraData)
                .lang("vi")
                .signature(signature)
                .build();

        try {
            String jsonBody = objectMapper.writeValueAsString(momoRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(momoProperties.getPayUrl()))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            MomoCreatePaymentResponse momoResponse = objectMapper.readValue(httpResponse.body(), MomoCreatePaymentResponse.class);

            if (momoResponse.resultCode() != null && momoResponse.resultCode() == 0) {
                return GatewayPaymentResult.builder()
                        .success(true)
                        .message("Khởi tạo thanh toán MoMo thành công")
                        .transactionReference(orderId)
                        .redirectUrl(momoResponse.payUrl())
                        .metadata(Map.of(
                                "payUrl", momoResponse.payUrl() != null ? momoResponse.payUrl() : "",
                                "deeplink", momoResponse.deeplink() != null ? momoResponse.deeplink() : "",
                                "qrCodeUrl", momoResponse.qrCodeUrl() != null ? momoResponse.qrCodeUrl() : ""
                        ))
                        .build();
            } else {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "MoMo từ chối tạo giao dịch: " + momoResponse.message());
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Lỗi khi kết nối tới MoMo API: {}", ex.getMessage(), ex);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể kết nối đến cổng MoMo: " + ex.getMessage());
        }
    }

    @Override
    public GatewayPaymentResult handleCallback(Map<String, String> callbackParams) {
        String signature = callbackParams.get("signature");
        if (signature == null || signature.isBlank()) {
            return GatewayPaymentResult.builder()
                    .success(false)
                    .message("Thiếu chữ ký signature từ MoMo")
                    .metadata(callbackParams)
                    .build();
        }

        String amount = callbackParams.getOrDefault("amount", "");
        String extraData = callbackParams.getOrDefault("extraData", "");
        String message = callbackParams.getOrDefault("message", "");
        String orderId = callbackParams.getOrDefault("orderId", "");
        String orderInfo = callbackParams.getOrDefault("orderInfo", "");
        String orderType = callbackParams.getOrDefault("orderType", "");
        String partnerCode = callbackParams.getOrDefault("partnerCode", "");
        String payType = callbackParams.getOrDefault("payType", "");
        String requestId = callbackParams.getOrDefault("requestId", "");
        String responseTime = callbackParams.getOrDefault("responseTime", "");
        String resultCode = callbackParams.getOrDefault("resultCode", "");
        String transId = callbackParams.getOrDefault("transId", "");

        // 1. Tái tạo rawSignature theo thứ tự callback MoMo:
        String rawSignature = "accessKey=" + momoProperties.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&message=" + message
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&orderType=" + orderType
                + "&partnerCode=" + partnerCode
                + "&payType=" + payType
                + "&requestId=" + requestId
                + "&responseTime=" + responseTime
                + "&resultCode=" + resultCode
                + "&transId=" + transId;

        // 2. So khớp chữ ký
        String expectedSignature = hmacSHA256(momoProperties.getSecretKey(), rawSignature);
        if (!expectedSignature.equalsIgnoreCase(signature)) {
            return GatewayPaymentResult.builder()
                    .success(false)
                    .message("Chữ ký MoMo không hợp lệ (Signature mismatch)")
                    .transactionReference(orderId)
                    .metadata(callbackParams)
                    .build();
        }

        // 3. Kiểm tra resultCode = "0" (Giao dịch thành công) hoặc "9000" (Đã được cấp phép)
        boolean isSuccess = "0".equals(resultCode) || "9000".equals(resultCode);

        return GatewayPaymentResult.builder()
                .success(isSuccess)
                .message(isSuccess ? "Thanh toán MoMo thành công" : "Thanh toán MoMo thất bại: " + message)
                .transactionReference(orderId)
                .metadata(callbackParams)
                .build();
    }

    public static String hmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tạo chữ ký HMAC-SHA256 MoMo", e);
        }
    }
}
