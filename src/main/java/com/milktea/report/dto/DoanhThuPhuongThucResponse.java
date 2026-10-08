package com.milktea.report.dto;

import java.math.BigDecimal;

public record DoanhThuPhuongThucResponse(
    String phuongThuc,
    String tenPhuongThuc,
    BigDecimal tongTien,
    Long soGiaoDich
) {}
