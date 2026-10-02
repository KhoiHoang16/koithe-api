package com.milktea.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PhieuNhapHangRequest(String maPhieuNhap, Long maNhaCungCap, Long maNguoiNhap, BigDecimal tongTien,
        String ghiChu, String trangThai, Instant ngayNhap, List<ChiTietPhieuNhapRequest> chiTiet) {
}
