package com.milktea.payment.gateway;

import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import java.util.Map;

/** Strategy contract for a payment method or external payment provider. */
public interface PaymentGateway {
    /**
     * Returns the payment method handled by this gateway.
     * The value must match {@code giao_dich_thanh_toan.phuong_thuc_thanh_toan}.
     *
     * @return stable method code such as {@code TIEN_MAT}, {@code VNPAY}, or {@code MOMO}
     */
    String getMethod();

    /**
     * Starts or completes a payment. Cash may complete immediately; an online provider can return
     * a redirect URL or QR payload while the payment remains pending.
     *
     * @param request order, amount, merchant reference, and callback/return details
     * @return provider result and any client-facing redirect or QR data
     */
    GatewayPaymentResult processPayment(GatewayPaymentRequest request);

    /**
     * Verifies and processes an asynchronous provider callback. Implementations must authenticate
     * the callback and tolerate duplicate delivery without applying a payment twice.
     *
     * @param callbackParams provider callback parameters
     * @return verified callback result
     */
    GatewayPaymentResult handleCallback(Map<String, String> callbackParams);
}
