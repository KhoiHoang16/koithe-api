package com.milktea.promotion.dto;

import java.math.BigDecimal;

public record KhuyenMaiSanPhamRequest(Long maChuongTrinh, Long maSanPham, Long maDanhMuc, String loaiGiamGia,
        BigDecimal giaTriGiam) {
}
