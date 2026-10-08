package com.milktea.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportSummaryResponse(
    LocalDate tuNgay,
    LocalDate denNgay,
    BigDecimal tongDoanhThu,
    Long tongSoDonHang,
    Long tongSoLyDaBan,
    List<DoanhThuTheoNgayResponse> doanhThuTheoNgay,
    List<DoanhThuKenhResponse> doanhThuTheoKenh,
    List<DoanhThuPhuongThucResponse> doanhThuTheoPhuongThuc,
    List<TopSanPhamResponse> topSanPhamBanChay
) {}
