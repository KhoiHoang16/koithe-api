package com.milktea.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GiaoDichThanhToanRequest(Long maDonHang, String phuongThucThanhToan, BigDecimal soTien,
        String noiDungVietqr, String trangThai, Instant thoiGianThanhToan) {
}
