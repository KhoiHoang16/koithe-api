package com.milktea.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GiaoDichThanhToanResponse(
        Long id,
        Long maDonHang,
        String phuongThucThanhToan,
        BigDecimal soTien,
        String noiDungVietqr,
        String trangThai,
        Instant thoiGianThanhToan,
        String redirectUrl) {

    public GiaoDichThanhToanResponse(
            Long id,
            Long maDonHang,
            String phuongThucThanhToan,
            BigDecimal soTien,
            String noiDungVietqr,
            String trangThai,
            Instant thoiGianThanhToan) {
        this(id, maDonHang, phuongThucThanhToan, soTien, noiDungVietqr, trangThai, thoiGianThanhToan, null);
    }
}
