package com.milktea.payment.gateway.impl;

import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QrTransferPaymentGateway implements PaymentGateway {
    @Override
    public String getMethod() {
        return "CHUYEN_KHOAN_QR";
    }

    @Override
    public GatewayPaymentResult processPayment(GatewayPaymentRequest request) {
        // TODO: [Partner] Sinh VietQR theo EMVCo với ngân hàng/tài khoản merchant từ cấu hình,
        // amount và nội dung chuyển khoản gắn với orderReference/transactionReference.
        // Không đánh dấu giao dịch thành công chỉ vì QR đã được tạo; chờ xác nhận tiền vào đáng tin cậy.
        throw new UnsupportedOperationException("TODO: Implement VietQR generation");
    }

    @Override
    public GatewayPaymentResult handleCallback(Map<String, String> callbackParams) {
        // TODO: [Partner] Tích hợp nguồn xác nhận chuyển khoản, xác thực chữ ký/nguồn gửi,
        // đối chiếu số tiền và mã tham chiếu, rồi xử lý callback lặp theo transaction reference.
        throw new UnsupportedOperationException("TODO: Implement QR transfer callback");
    }
}
