package com.milktea.promotion.dto;

import com.milktea.promotion.entity.LoaiGiamGia;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record KhuyenMaiVoucherRequest(
        @NotNull(message = "Mã chương trình không được để trống")
        Long maChuongTrinh,

        @NotBlank(message = "Mã voucher không được để trống")
        String maCode,

        @NotNull(message = "Loại giảm giá không được để trống")
        LoaiGiamGia loaiGiamGia,

        @NotNull(message = "Giá trị giảm không được để trống")
        @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
        BigDecimal giaTriGiam,

        @DecimalMin(value = "0.0", message = "Mức giảm tối đa không được âm")
        BigDecimal mucGiamToiDa,

        @NotNull(message = "Đơn hàng tối thiểu không được để trống")
        @DecimalMin(value = "0.0", message = "Đơn hàng tối thiểu không được âm")
        BigDecimal donHangToiThieu,

        @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
        Integer gioiHanSuDung,

        Integer soLuotDaDung
) {
}
