package com.milktea.payment.gateway.impl;

import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CashPaymentGateway implements PaymentGateway {
    @Override
    public String getMethod() {
        return "TIEN_MAT";
    }

    @Override
    public GatewayPaymentResult processPayment(GatewayPaymentRequest request) {
        return GatewayPaymentResult.builder()
                .success(true)
                .message("Thanh toán tiền mặt thành công")
                .transactionReference(request.transactionReference())
                .build();
    }

    @Override
    public GatewayPaymentResult handleCallback(Map<String, String> callbackParams) {
        return GatewayPaymentResult.builder()
                .success(false)
                .message("Tiền mặt không hỗ trợ callback")
                .build();
    }
}
