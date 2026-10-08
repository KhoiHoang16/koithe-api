package com.milktea.payment.gateway.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.VietQrProperties;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Xử lý thanh toán Chuyển khoản VietQR qua chuẩn Napas 247 và Webhook đối soát biến động số dư.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QrTransferPaymentGateway implements PaymentGateway {
    private final VietQrProperties vietQrProperties;

    @Override
    public String getMethod() {
        return "CHUYEN_KHOAN_QR";
    }

    @Override
    public GatewayPaymentResult processPayment(GatewayPaymentRequest request) {
        String orderRef = request.orderReference() != null && !request.orderReference().isBlank()
                ? request.orderReference().replace("-", "")
                : "DH" + request.orderId();

        String txnRef = request.orderId() + "_" + System.currentTimeMillis();
        long amount = request.amount().longValue();

        try {
            // Xây dựng URL ảnh mã VietQR động theo chuẩn QuickLink của VietQR.io:
            // Format: https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<CONTENT>&accountName=<NAME>
            String encodedContent = URLEncoder.encode(orderRef, StandardCharsets.UTF_8.toString());
            String encodedAccountName = URLEncoder.encode(vietQrProperties.getAccountName(), StandardCharsets.UTF_8.toString());

            String qrImageUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                    vietQrProperties.getBankId(),
                    vietQrProperties.getAccountNo(),
                    vietQrProperties.getTemplate(),
                    amount,
                    encodedContent,
                    encodedAccountName
            );

            log.info("Đã tạo VietQR thành công cho đơn hàng #{} [{}], URL: {}", request.orderId(), orderRef, qrImageUrl);

            return GatewayPaymentResult.builder()
                    .success(true)
                    .message("Tạo mã VietQR thành công")
                    .transactionReference(txnRef)
                    .redirectUrl(qrImageUrl)
                    .metadata(Map.of(
                            "qrImageUrl", qrImageUrl,
                            "transferContent", orderRef,
                            "accountNo", vietQrProperties.getAccountNo(),
                            "bankId", vietQrProperties.getBankId(),
                            "accountName", vietQrProperties.getAccountName()
                    ))
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi tạo URL mã VietQR: {}", e.getMessage(), e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo mã QR chuyển khoản: " + e.getMessage());
        }
    }

    @Override
    public GatewayPaymentResult handleCallback(Map<String, String> callbackParams) {
        // 1. Xác thực Webhook Secret Token (chống kẻ xấu gọi fake webhook)
        String secretToken = callbackParams.get("secretToken");
        if (secretToken == null || !secretToken.equals(vietQrProperties.getWebhookSecret())) {
            log.warn("VietQR Webhook bị từ chối: Sai secret token");
            return GatewayPaymentResult.builder()
                    .success(false)
                    .message("Webhook secret token không hợp lệ")
                    .metadata(callbackParams)
                    .build();
        }

        // 2. Lấy thông tin giao dịch nhận tiền
        String content = callbackParams.getOrDefault("content", "");
        String amountStr = callbackParams.getOrDefault("amount", "0");
        String txnRef = callbackParams.getOrDefault("transactionReference", "");

        // 3. Trích xuất orderId từ transactionReference hoặc nội dung chuyển khoản
        String resolvedOrderId = "";
        if (txnRef != null && !txnRef.isBlank()) {
            resolvedOrderId = txnRef;
        } else if (content.toUpperCase().contains("DH")) {
            int index = content.toUpperCase().indexOf("DH");
            String sub = content.substring(index + 2);
            StringBuilder digits = new StringBuilder();
            for (char c : sub.toCharArray()) {
                if (Character.isDigit(c)) {
                    digits.append(c);
                } else if (c != '-' && !digits.isEmpty()) {
                    break;
                }
            }
            resolvedOrderId = digits.toString();
        }

        if (resolvedOrderId.isBlank()) {
            return GatewayPaymentResult.builder()
                    .success(false)
                    .message("Không tìm thấy mã đơn hàng trong nội dung chuyển khoản: " + content)
                    .metadata(callbackParams)
                    .build();
        }

        log.info("Xác thực VietQR thành công cho mã đơn/giao dịch: {}, số tiền: {}", resolvedOrderId, amountStr);

        return GatewayPaymentResult.builder()
                .success(true)
                .message("Chuyển khoản VietQR thành công")
                .transactionReference(resolvedOrderId)
                .metadata(callbackParams)
                .build();
    }
}
