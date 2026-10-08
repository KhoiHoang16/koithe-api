package com.milktea.report.dto;

import java.math.BigDecimal;

public record TopSanPhamResponse(
    Long bienTheId,
    String tenSanPham,
    String kichCo,
    Long soLuongDaBan,
    BigDecimal tongTien
) {}
