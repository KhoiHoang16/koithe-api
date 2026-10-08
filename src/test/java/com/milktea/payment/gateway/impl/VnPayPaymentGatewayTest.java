package com.milktea.payment.gateway.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.milktea.payment.gateway.VnPayProperties;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VnPayPaymentGatewayTest {
    VnPayProperties properties;
    VnPayPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        properties = new VnPayProperties();
        properties.setTmnCode("2QXUI4B4");
        properties.setHashSecret("RAOCTAV2VYRW19J77TIFOUF9NE0U0DBC");
        properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        properties.setReturnUrl("http://localhost:8088/api/payments/vnpay-return");
        gateway = new VnPayPaymentGateway(properties);
    }

    @Test
    void processPayment_GeneratesValidUrl() {
        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .orderId(101L)
                .amount(new BigDecimal("45000"))
                .description("Thanh toan don hang #101")
                .build();

        GatewayPaymentResult result = gateway.processPayment(request);

        assertTrue(result.success());
        assertNotNull(result.redirectUrl());
        assertTrue(result.redirectUrl().startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(result.redirectUrl().contains("vnp_Amount=4500000"));
        assertTrue(result.redirectUrl().contains("vnp_TmnCode=2QXUI4B4"));
        assertTrue(result.redirectUrl().contains("vnp_SecureHash="));
    }

    @Test
    void handleCallback_ValidSignature_Success() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TmnCode", "2QXUI4B4");
        params.put("vnp_Amount", "4500000");
        params.put("vnp_TxnRef", "101_123456789");
        params.put("vnp_OrderInfo", "Thanh toan don hang #101");
        params.put("vnp_ResponseCode", "00");

        // Compute valid signature for testing
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String field = fieldNames.get(i);
            hashData.append(field).append('=').append(URLEncoder.encode(params.get(field), StandardCharsets.US_ASCII));
            if (i < fieldNames.size() - 1) hashData.append('&');
        }
        String secureHash = VnPayPaymentGateway.hmacSHA512("RAOCTAV2VYRW19J77TIFOUF9NE0U0DBC", hashData.toString());
        params.put("vnp_SecureHash", secureHash);

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertTrue(result.success());
        assertEquals("Thanh toán VNPay thành công", result.message());
        assertEquals("101_123456789", result.transactionReference());
    }

    @Test
    void handleCallback_InvalidSignature_Fails() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "4500000");
        params.put("vnp_TxnRef", "101_123456789");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_SecureHash", "INVALID_HASH_VALUE_HERE");

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertFalse(result.success());
        assertTrue(result.message().contains("không hợp lệ"));
    }
}
