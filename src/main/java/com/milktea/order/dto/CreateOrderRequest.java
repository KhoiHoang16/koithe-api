package com.milktea.order.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(String cartToken, @Positive Long customerId,
        @Pattern(regexp = "TAI_CHO|MANG_VE|GIAO_HANG") String serviceType,
        @Pattern(regexp = "TAI_QUAY_POS|QUET_QR_BAN|UNG_DUNG_APP|WEBSITE") String channel,
        @Positive Long tableId) {}
