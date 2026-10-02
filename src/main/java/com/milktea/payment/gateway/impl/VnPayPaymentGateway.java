package com.milktea.payment.gateway.impl;

import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class VnPayPaymentGateway implements PaymentGateway {
    @Override
    public String getMethod() {
        return "VNPAY";
    }

    @Override
    public GatewayPaymentResult processPayment(GatewayPaymentRequest request) {
        // TODO: [Partner] Tích hợp VNPay theo tài liệu chính thức:
        // https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html
        // 1. Tạo URL với vnp_Amount, vnp_TxnRef, vnp_OrderInfo, vnp_ReturnUrl và cấu hình merchant.
        // 2. Chuẩn hóa/sắp xếp tham số đúng spec, ký HMAC-SHA512 bằng vnp_HashSecret; không hard-code secret.
        // 3. Trả URL redirect và transaction reference; lưu giao dịch ở trạng thái CHO_XU_LY.
        throw new UnsupportedOperationException("TODO: Implement VNPay integration");
    }

    @Override
    public GatewayPaymentResult handleCallback(Map<String, String> callbackParams) {
        // TODO: [Partner] Xử lý IPN/return theo tài liệu VNPay.
        // 1. Tính lại và so sánh vnp_SecureHash trước khi tin bất kỳ callback field nào.
        // 2. Kiểm tra vnp_ResponseCode, amount, merchant reference và transaction reference.
        // 3. Cập nhật trạng thái trong transaction; xử lý idempotency để callback lặp không ghi nhận hai lần.
        throw new UnsupportedOperationException("TODO: Implement VNPay callback");
    }
}
