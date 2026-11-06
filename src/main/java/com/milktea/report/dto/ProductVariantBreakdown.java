package com.milktea.report.dto;

import java.math.BigDecimal;

public record ProductVariantBreakdown(
    Long bienTheId,
    String kichCo,
    Long soLuong,
    BigDecimal doanhThu
) {}
