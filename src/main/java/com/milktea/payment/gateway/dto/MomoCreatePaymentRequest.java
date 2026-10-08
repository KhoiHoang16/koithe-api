package com.milktea.payment.gateway.dto;

import lombok.Builder;

/**
 * Payload JSON gửi sang MoMo API để khởi tạo giao dịch thanh toán.
 */
@Builder
public record MomoCreatePaymentRequest(
        String partnerCode,
        String requestId,
        Long amount,
        String orderId,
        String orderInfo,
        String redirectUrl,
        String ipnUrl,
        String requestType,
        String extraData,
        String lang,
        String signature
) {}
