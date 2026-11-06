package com.milktea.report.dto;

import java.math.BigDecimal;

public record RevenueTrendResponse(
    String periodKey,
    String label,
    BigDecimal tongDoanhThu,
    Long tongSoDon,
    Long tongSoLy
) {}
