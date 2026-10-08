package com.milktea.payment.gateway.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.payment.gateway.MomoProperties;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MomoPaymentGatewayTest {
    MomoProperties properties;
    ObjectMapper objectMapper;
    HttpClient httpClient;
    MomoPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        properties = new MomoProperties();
        properties.setPartnerCode("MOMOBKUN20180529");
        properties.setAccessKey("klm05TvNBzhg7h7j");
        properties.setSecretKey("at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa");
        properties.setPayUrl("https://test-payment.momo.vn/v2/gateway/api/create");
        properties.setReturnUrl("http://localhost:8088/api/payments/momo-return");
        properties.setIpnUrl("http://localhost:8088/api/payments/momo-ipn");

        objectMapper = new ObjectMapper();
        httpClient = mock(HttpClient.class);
        gateway = new MomoPaymentGateway(properties, objectMapper, httpClient);
    }

    @Test
    void getMethod_ReturnsMOMO() {
        assertEquals("MOMO", gateway.getMethod());
    }

    @Test
    void processPayment_Success() throws IOException, InterruptedException {
        String mockResponseBody = """
                {
                    "partnerCode": "MOMO",
                    "orderId": "101_123456",
                    "requestId": "101_123456_789",
                    "amount": 35000,
                    "responseTime": 1700000000,
                    "message": "Thành công",
                    "resultCode": 0,
                    "payUrl": "https://test-payment.momo.vn/v2/gateway/pay?token=xyz123",
                    "deeplink": "momo://?action=payWithApp&token=xyz123",
                    "qrCodeUrl": "https://test-payment.momo.vn/qr/xyz123"
                }
                """;

        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        doReturn(mockResponse).when(httpClient).send(any(HttpRequest.class), any());

        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .orderId(101L)
                .amount(new BigDecimal("35000"))
                .description("Thanh toan don hang #101")
                .build();

        GatewayPaymentResult result = gateway.processPayment(request);

        assertTrue(result.success());
        assertEquals("https://test-payment.momo.vn/v2/gateway/pay?token=xyz123", result.redirectUrl());
        assertNotNull(result.transactionReference());
        verify(httpClient, times(1)).send(any(HttpRequest.class), any());
    }

    @Test
    void handleCallback_ValidSignature_Success() {
        Map<String, String> params = new HashMap<>();
        params.put("partnerCode", "MOMO");
        params.put("orderId", "101_123456");
        params.put("requestId", "101_123456_789");
        params.put("amount", "35000");
        params.put("orderInfo", "Thanh toan don hang #101");
        params.put("orderType", "momo_wallet");
        params.put("transId", "2300000123");
        params.put("resultCode", "0");
        params.put("message", "Giao dịch thành công");
        params.put("payType", "qr");
        params.put("responseTime", "1700000000");
        params.put("extraData", "");

        // Tái tạo rawSignature theo chuẩn MoMo
        String rawSignature = "accessKey=" + properties.getAccessKey()
                + "&amount=" + params.get("amount")
                + "&extraData=" + params.get("extraData")
                + "&message=" + params.get("message")
                + "&orderId=" + params.get("orderId")
                + "&orderInfo=" + params.get("orderInfo")
                + "&orderType=" + params.get("orderType")
                + "&partnerCode=" + params.get("partnerCode")
                + "&payType=" + params.get("payType")
                + "&requestId=" + params.get("requestId")
                + "&responseTime=" + params.get("responseTime")
                + "&resultCode=" + params.get("resultCode")
                + "&transId=" + params.get("transId");

        String validSignature = MomoPaymentGateway.hmacSHA256(properties.getSecretKey(), rawSignature);
        params.put("signature", validSignature);

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertTrue(result.success());
        assertEquals("Thanh toán MoMo thành công", result.message());
        assertEquals("101_123456", result.transactionReference());
    }

    @Test
    void handleCallback_InvalidSignature_Fails() {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", "101_123456");
        params.put("amount", "35000");
        params.put("resultCode", "0");
        params.put("signature", "invalid_signature_hex_123456");

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertFalse(result.success());
        assertTrue(result.message().contains("Chữ ký MoMo không hợp lệ"));
    }

    @Test
    void handleCallback_MissingSignature_Fails() {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", "101_123456");

        GatewayPaymentResult result = gateway.handleCallback(params);

        assertFalse(result.success());
        assertTrue(result.message().contains("Thiếu chữ ký"));
    }
}
