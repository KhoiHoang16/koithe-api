package com.milktea.payment.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình thông số tích hợp cổng thanh toán VNPay (Sandbox / Production).
 */
@Configuration
@ConfigurationProperties(prefix = "app.vnpay")
@Getter
@Setter
public class VnPayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
}
