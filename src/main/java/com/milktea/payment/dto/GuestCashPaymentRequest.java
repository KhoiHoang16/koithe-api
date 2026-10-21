package com.milktea.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record GuestCashPaymentRequest(
        @NotNull Long maDonHang,
        @NotBlank String cartToken,
        @NotNull @PositiveOrZero BigDecimal soTien) {
}
