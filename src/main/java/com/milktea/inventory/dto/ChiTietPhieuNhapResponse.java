package com.milktea.inventory.dto;

import java.math.BigDecimal;

public record ChiTietPhieuNhapResponse(Long id, String tenMatHang, String donViTinh, BigDecimal soLuong,
        BigDecimal donGiaNhap, BigDecimal thanhTien) {
}
