package com.milktea.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DoanhThuTheoNgayResponse(
    LocalDate ngay,
    BigDecimal doanhThu,
    Long soDon
) {}
