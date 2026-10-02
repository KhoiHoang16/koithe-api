package com.milktea.payment.gateway.dto;

import java.math.BigDecimal;
import lombok.Builder;

/** Data passed from the payment module to one gateway strategy. */
@Builder
public record GatewayPaymentRequest(
        Long paymentId,
        Long orderId,
        String orderReference,
        String transactionReference,
        BigDecimal amount,
        String description,
        String returnUrl,
        String callbackUrl) {
}
