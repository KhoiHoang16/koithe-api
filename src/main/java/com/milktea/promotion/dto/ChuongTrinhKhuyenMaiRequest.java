package com.milktea.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ChuongTrinhKhuyenMaiRequest(
        @NotBlank(message = "Tên chương trình không được để trống")
        String tenChuongTrinh,

        String moTa,

        @NotNull(message = "Ngày bắt đầu không được để trống")
        Instant ngayBatDau,

        @NotNull(message = "Ngày kết thúc không được để trống")
        Instant ngayKetThuc,

        @NotNull(message = "Trạng thái hoạt động không được để trống")
        Boolean dangHoatDong
) {
}
