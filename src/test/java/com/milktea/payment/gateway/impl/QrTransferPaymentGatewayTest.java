package com.milktea.payment.gateway.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.milktea.payment.gateway.VietQrProperties;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QrTransferPaymentGatewayTest {
    private VietQrProperties properties;
    private QrTransferPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        properties = new VietQrProperties();
        properties.setBankId("MB");
        properties.setAccountNo("0987654321");
        properties.setAccountName("KOI THE MILKTEA");
        properties.setTemplate("compact2");
        properties.setWebhookSecret("test_secret_123");

        gateway = new QrTransferPaymentGateway(properties);
    }

    @Test
    void processPayment_ReturnsValidVietQrImageUrl() {
        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .orderId(5L)
                .orderReference("DH-2026-005")
                .amount(new BigDecimal("35000.00"))
                .build();

        GatewayPaymentResult result = gateway.processPayment(request);

        assertNotNull(result);
        assertTrue(result.success());
        assertNotNull(result.redirectUrl());
        assertTrue(result.redirectUrl().contains("img.vietqr.io/image/MB-0987654321-compact2.png"));
        assertTrue(result.redirectUrl().contains("amount=35000"));
        assertTrue(result.redirectUrl().contains("DH2026005"));
        assertEquals("CHUYEN_KHOAN_QR", gateway.getMethod());
    }

    @Test
    void handleCallback_ValidSecret_Success() {
        Map<String, String> params = Map.of(
                "secretToken", "test_secret_123",
                "transactionReference", "5_17912345",
                "amount", "35000",
                "content", "DH2026005 chuyen khoan"
        );

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals("5_17912345", result.transactionReference());
    }

    @Test
    void handleCallback_ExtractFromContent_Success() {
        Map<String, String> params = Map.of(
                "secretToken", "test_secret_123",
                "amount", "35000",
                "content", "DH5 Nguyen Van A chuyen khoan"
        );

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals("5", result.transactionReference());
    }

    @Test
    void handleCallback_InvalidSecret_Fails() {
        Map<String, String> params = Map.of(
                "secretToken", "wrong_secret",
                "transactionReference", "5_17912345",
                "amount", "35000"
        );

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertNotNull(result);
        assertFalse(result.success());
        assertEquals("Webhook secret token không hợp lệ", result.message());
    }

    @Test
    void handleCallback_MissingOrderId_Fails() {
        Map<String, String> params = Map.of(
                "secretToken", "test_secret_123",
                "amount", "35000",
                "content", "khong co ma don hang nao ca"
        );

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertNotNull(result);
        assertFalse(result.success());
        assertTrue(result.message().contains("Không tìm thấy mã đơn hàng"));
    }
}
