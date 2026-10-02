package com.milktea.payment.gateway.dto;

import java.util.Map;
import lombok.Builder;

/** Normalized result returned by a payment gateway strategy. */
@Builder
public record GatewayPaymentResult(
        boolean success,
        String message,
        String transactionReference,
        String redirectUrl,
        String qrCode,
        Map<String, String> metadata) {
}
