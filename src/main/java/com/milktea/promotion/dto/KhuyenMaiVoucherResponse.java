package com.milktea.promotion.dto;

import com.milktea.promotion.entity.LoaiGiamGia;
import java.math.BigDecimal;

public record KhuyenMaiVoucherResponse(
        Long id,
        Long maChuongTrinh,
        String maCode,
        LoaiGiamGia loaiGiamGia,
        BigDecimal giaTriGiam,
        BigDecimal mucGiamToiDa,
        BigDecimal donHangToiThieu,
        Integer gioiHanSuDung,
        Integer soLuotDaDung
) {
}
