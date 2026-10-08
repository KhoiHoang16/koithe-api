package com.milktea.promotion.dto;

import com.milktea.promotion.entity.LoaiGiamGia;
import java.math.BigDecimal;

public record KhuyenMaiHoaDonResponse(
        Long id,
        Long maChuongTrinh,
        BigDecimal donHangToiThieu,
        LoaiGiamGia loaiGiamGia,
        BigDecimal giaTriGiam,
        BigDecimal mucGiamToiDa
) {
}
