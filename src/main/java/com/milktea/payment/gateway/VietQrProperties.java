package com.milktea.payment.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình thông số tài khoản thụ hưởng VietQR và Webhook đối soát.
 */
@Configuration
@ConfigurationProperties(prefix = "app.vietqr")
@Getter
@Setter
public class VietQrProperties {
    /**
     * Mã định danh ngân hàng thụ hưởng theo chuẩn Napas/VietQR (ví dụ: "MB", "ICB", "VCB", "ACB" hoặc mã BIN "970422").
     */
    private String bankId = "MB";

    /**
     * Số tài khoản ngân hàng thụ hưởng của cửa hàng.
     */
    private String accountNo = "0987654321";

    /**
     * Tên chủ tài khoản hiển thị trên mã QR (viết hoa không dấu).
     */
    private String accountName = "KOI THE MILKTEA";

    /**
     * Template hiển thị mã QR của VietQR: "compact2", "compact", "qr_only", "print".
     */
    private String template = "compact2";

    /**
     * Khóa bí mật (Token/Secret) dùng để xác thực webhook gửi từ dịch vụ biến động số dư / Open Banking.
     */
    private String webhookSecret = "vietqr_dev_webhook_secret_key_2026";
}
