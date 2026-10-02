package com.milktea.shift.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CaLamViecResponse(Long id, Instant thoiGianBatDau, Instant thoiGianKetThuc, BigDecimal tienDauCa,
        BigDecimal tienKetCa, String trangThai) {
}
