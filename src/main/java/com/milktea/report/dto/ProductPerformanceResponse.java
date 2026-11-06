package com.milktea.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductPerformanceResponse(
    Long sanPhamId,
    String tenSanPham,
    String danhMuc,
    String hinhAnh,
    Long tongSoLyDaBan,
    BigDecimal tongDoanhThu,
    Double tyTrongDoanhThu,
    List<ProductVariantBreakdown> chiTietBienThe
) {}
