package com.milktea.inventory.dto;

import java.math.BigDecimal;

public record ChiTietPhieuNhapRequest(Long maPhieuNhap, Long maBienThe, Long maTopping, String tenMatHang,
        String donViTinh, BigDecimal soLuong, BigDecimal donGiaNhap, BigDecimal thanhTien) {
}
