package com.milktea.order.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(String cartToken, @Positive Long customerId,
        @Pattern(regexp = "TAI_CHO|MANG_VE|GIAO_HANG") String serviceType,
        @Pattern(regexp = "TAI_QUAY_POS|QUET_QR_BAN|UNG_DUNG_APP|WEBSITE") String channel,
        @Positive Long tableId,
        @Size(max = 255) String tenNguoiNhan,
        @Size(max = 30) String soDienThoaiNguoiNhan,
        @Size(max = 1000) String diaChiGiaoHang) {}
