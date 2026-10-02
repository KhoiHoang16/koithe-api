package com.milktea.shift.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CaLamViecRequest(Long maThuNgan, Instant thoiGianBatDau, Instant thoiGianKetThuc, BigDecimal tienDauCa,
        BigDecimal tienKetCa, String trangThai) {
}
