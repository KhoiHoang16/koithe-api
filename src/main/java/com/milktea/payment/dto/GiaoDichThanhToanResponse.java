package com.milktea.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GiaoDichThanhToanResponse(Long id, String phuongThucThanhToan, BigDecimal soTien, String noiDungVietqr,
        String trangThai, Instant thoiGianThanhToan) {
}
