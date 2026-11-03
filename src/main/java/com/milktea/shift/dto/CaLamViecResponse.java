package com.milktea.shift.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CaLamViecResponse(
        Long id,
        Long maThuNgan,
        String tenThuNgan,
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
        this(id, null, null, thoiGianBatDau, thoiGianKetThuc, tienDauCa, tienKetCa, trangThai, null, null, null, null);
    }

    public CaLamViecResponse(Long id, Instant thoiGianBatDau, Instant thoiGianKetThuc, BigDecimal tienDauCa, BigDecimal tienKetCa, String trangThai, BigDecimal tongDoanhThu, Long soDonHang, BigDecimal tienMatDuKien, BigDecimal chenhLech) {
        this(id, null, null, thoiGianBatDau, thoiGianKetThuc, tienDauCa, tienKetCa, trangThai, tongDoanhThu, soDonHang, tienMatDuKien, chenhLech);
    }
}
