package com.milktea.promotion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ValidateVoucherRequest(
        @NotBlank(message = "Mã voucher không được để trống")
        String maCode,

        @NotNull(message = "Tổng tiền đơn hàng không được để trống")
        @DecimalMin(value = "0.0", message = "Tổng tiền đơn hàng không được âm")
        BigDecimal tongTienDonHang
) {
}
