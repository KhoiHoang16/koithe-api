package com.milktea.promotion.dto;

import com.milktea.promotion.entity.LoaiGiamGia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record KhuyenMaiSanPhamRequest(
        @NotNull(message = "Mã chương trình không được để trống")
        Long maChuongTrinh,

        Long maSanPham,

        Long maDanhMuc,

        @NotNull(message = "Loại giảm giá không được để trống")
        LoaiGiamGia loaiGiamGia,

        @NotNull(message = "Giá trị giảm không được để trống")
        @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
        BigDecimal giaTriGiam
) {
}
