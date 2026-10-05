package com.milktea.shift.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CaLamViecRequest(
        @JsonAlias({"cashierId", "ma_thu_ngan", "nhanVienId", "staffId"}) Long maThuNgan,
        @JsonAlias({"startTime", "thoi_gian_bat_dau"}) Instant thoiGianBatDau,
        @JsonAlias({"endTime", "thoi_gian_ket_thuc"}) Instant thoiGianKetThuc,
        @JsonAlias({"startingCash", "tien_dau_ca", "initialCash"}) BigDecimal tienDauCa,
        @JsonAlias({"endingCash", "tien_ket_ca", "finalCash"}) BigDecimal tienKetCa,
        @JsonAlias({"status", "trang_thai"}) String trangThai) {
}
