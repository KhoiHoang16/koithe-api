package com.milktea.promotion.dto;

import java.math.BigDecimal;

public record ValidateVoucherResponse(
        String maCode,
        boolean hopLe,
        BigDecimal soTienGiam,
        BigDecimal tongTienSauGiam,
        String thongDiep
) {
}
