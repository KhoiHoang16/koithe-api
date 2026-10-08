package com.milktea.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;

public record GiaoDichThanhToanRequest(
        @NotNull(message = "Mã đơn hàng không được để trống")
        Long maDonHang,

        @NotBlank(message = "Phương thức thanh toán không được để trống")
        String phuongThucThanhToan,

        @NotNull(message = "Số tiền không được để trống")
        @PositiveOrZero(message = "Số tiền thanh toán không được âm")
        BigDecimal soTien,

        String noiDungVietqr,
        String trangThai,
        Instant thoiGianThanhToan) {
}
