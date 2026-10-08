package com.milktea.payment.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Phản hồi JSON nhận được từ MoMo API sau khi tạo thanh toán.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MomoCreatePaymentResponse(
        String partnerCode,
        String orderId,
        String requestId,
        Long amount,
        Long responseTime,
        String message,
        Integer resultCode,
        String payUrl,
        String deeplink,
        String qrCodeUrl,
        String signature
) {}
