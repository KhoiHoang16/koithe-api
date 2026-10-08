package com.milktea.promotion.dto;

import com.milktea.promotion.entity.LoaiGiamGia;
import java.math.BigDecimal;

public record KhuyenMaiSanPhamResponse(
        Long id,
        Long maChuongTrinh,
        Long maSanPham,
        Long maDanhMuc,
        LoaiGiamGia loaiGiamGia,
        BigDecimal giaTriGiam
) {
}
