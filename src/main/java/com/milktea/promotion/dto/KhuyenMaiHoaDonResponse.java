package com.milktea.promotion.dto;

import java.math.BigDecimal;

public record KhuyenMaiHoaDonResponse(Long id, BigDecimal donHangToiThieu, String loaiGiamGia, BigDecimal giaTriGiam,
        BigDecimal mucGiamToiDa) {
}
