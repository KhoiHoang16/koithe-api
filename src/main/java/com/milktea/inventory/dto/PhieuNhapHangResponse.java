package com.milktea.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PhieuNhapHangResponse(Long id, String maPhieuNhap, BigDecimal tongTien, String ghiChu, String trangThai,
        Instant ngayNhap) {
}
