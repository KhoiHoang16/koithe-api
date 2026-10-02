package com.milktea.promotion.dto;

import java.time.Instant;

public record ChuongTrinhKhuyenMaiResponse(Long id, String tenChuongTrinh, String moTa, Instant ngayBatDau,
        Instant ngayKetThuc, Boolean dangHoatDong) {
}
