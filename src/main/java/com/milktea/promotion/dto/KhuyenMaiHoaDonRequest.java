package com.milktea.promotion.dto;

import com.milktea.promotion.entity.LoaiGiamGia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record KhuyenMaiHoaDonRequest(
        @NotNull(message = "Mã chương trình không được để trống")
        Long maChuongTrinh,

        @NotNull(message = "Đơn hàng tối thiểu không được để trống")
        @DecimalMin(value = "0.0", message = "Đơn hàng tối thiểu không được âm")
        BigDecimal donHangToiThieu,

        @NotNull(message = "Loại giảm giá không được để trống")
        LoaiGiamGia loaiGiamGia,

        @NotNull(message = "Giá trị giảm không được để trống")
        @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
        BigDecimal giaTriGiam,

        @DecimalMin(value = "0.0", message = "Mức giảm tối đa không được âm")
        BigDecimal mucGiamToiDa
) {
}
