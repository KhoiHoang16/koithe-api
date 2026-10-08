package com.milktea.payment.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình thông số tích hợp cổng thanh toán MoMo (Sandbox / Production).
 */
@Configuration
@ConfigurationProperties(prefix = "app.momo")
@Getter
@Setter
public class MomoProperties {
    private String partnerCode;
    private String accessKey;
    private String secretKey;
    private String payUrl;
    private String returnUrl;
    private String ipnUrl;
}
