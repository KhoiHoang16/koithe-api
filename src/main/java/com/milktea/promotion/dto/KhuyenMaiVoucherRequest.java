package com.milktea.promotion.dto;

import java.math.BigDecimal;

public record KhuyenMaiVoucherRequest(Long maChuongTrinh, String maCode, String loaiGiamGia, BigDecimal giaTriGiam,
        BigDecimal mucGiamToiDa, BigDecimal donHangToiThieu, Integer gioiHanSuDung, Integer soLuotDaDung) {
}
