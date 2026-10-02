package com.milktea.payment.gateway.impl;

import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MomoPaymentGateway implements PaymentGateway {
    @Override
    public String getMethod() {
        return "MOMO";
    }

    @Override
    public GatewayPaymentResult processPayment(GatewayPaymentRequest request) {
        // TODO: [Partner] Tích hợp MoMo theo tài liệu chính thức:
        // https://developers.momo.vn/v3/docs/payment/api/collection-link/
        // 1. Tạo requestId/orderId duy nhất và payload theo đúng endpoint/sản phẩm đã đăng ký.
        // 2. Ký request bằng HMAC_SHA256 theo spec MoMo, gửi bằng HTTPS và kiểm tra response signature.
        // 3. Trả payUrl/deeplink cho client và giữ giao dịch ở trạng thái CHO_XU_LY.
        throw new UnsupportedOperationException("TODO: Implement MoMo integration");
    }

    @Override
    public GatewayPaymentResult handleCallback(Map<String, String> callbackParams) {
        // TODO: [Partner] Xử lý IPN/callback MoMo.
        // 1. Xác thực signature bằng secret lấy từ cấu hình bảo mật, không tin callback chưa xác thực.
        // 2. Đối chiếu orderId, requestId, amount và resultCode với giao dịch đã khởi tạo.
        // 3. Xử lý callback lặp một cách idempotent và chỉ chuyển trạng thái đơn khi khoản thu hợp lệ.
        throw new UnsupportedOperationException("TODO: Implement MoMo callback");
    }
}
