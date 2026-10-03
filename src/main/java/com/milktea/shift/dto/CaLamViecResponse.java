package com.milktea.shift.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CaLamViecResponse(
        Long id,
        Instant thoiGianBatDau,
        Instant thoiGianKetThuc,
        BigDecimal tienDauCa,
        BigDecimal tienKetCa,
        String trangThai,
        BigDecimal tongDoanhThu,
        Long soDonHang,
        BigDecimal tienMatDuKien,
        BigDecimal chenhLech) {

    public CaLamViecResponse(Long id, Instant thoiGianBatDau, Instant thoiGianKetThuc, BigDecimal tienDauCa, BigDecimal tienKetCa, String trangThai) {
        this(id, thoiGianBatDau, thoiGianKetThuc, tienDauCa, tienKetCa, trangThai, null, null, null, null);
    }
}
