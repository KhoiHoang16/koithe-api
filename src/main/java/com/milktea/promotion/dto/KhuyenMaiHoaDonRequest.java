package com.milktea.promotion.dto;

import java.math.BigDecimal;

public record KhuyenMaiHoaDonRequest(Long maChuongTrinh, BigDecimal donHangToiThieu, String loaiGiamGia,
        BigDecimal giaTriGiam, BigDecimal mucGiamToiDa) {
}
