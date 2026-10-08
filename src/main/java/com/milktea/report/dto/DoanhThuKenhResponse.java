package com.milktea.report.dto;

import java.math.BigDecimal;

public record DoanhThuKenhResponse(
    String maKenh,
    String tenKenh,
    BigDecimal doanhThu,
    Long soDon
) {}
