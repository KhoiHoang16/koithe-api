package com.milktea.report.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.milktea.common.response.PageResponse;
import com.milktea.report.dto.*;
import com.milktea.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    ReportRepository reportRepository;

    ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(reportRepository);
    }

    @Test
    @DisplayName("Thành công: Lấy báo cáo theo khoảng ngày hợp lệ")
    void getSummary_validRange_success() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 8);

        when(reportRepository.sumRevenueAndCountOrders(any(), any(), any()))
                .thenReturn(Collections.singletonList(new Object[]{new BigDecimal("1500000.00"), 10L}));
        when(reportRepository.sumTotalCupsSold(any(), any(), any()))
                .thenReturn(25L);
        when(reportRepository.dailyRevenue(any(), any(), any()))
                .thenReturn(List.of(
                        new Object[]{Date.valueOf(LocalDate.of(2026, 10, 1)), new BigDecimal("500000.00"), 3L},
                        new Object[]{Date.valueOf(LocalDate.of(2026, 10, 2)), new BigDecimal("1000000.00"), 7L}
                ));
        when(reportRepository.revenueByChannel(any(), any(), any()))
                .thenReturn(List.of(
                        new Object[]{"TAI_QUAY_POS", new BigDecimal("1000000.00"), 6L},
                        new Object[]{"QUET_QR_BAN", new BigDecimal("300000.00"), 2L},
                        new Object[]{"WEBSITE", new BigDecimal("200000.00"), 2L}
                ));
        when(reportRepository.revenueByPaymentMethod(any(), any(), any()))
                .thenReturn(List.of(
                        new Object[]{"TIEN_MAT", new BigDecimal("700000.00"), 5L},
                        new Object[]{"CHUYEN_KHOAN_QR", new BigDecimal("500000.00"), 3L},
                        new Object[]{"MOMO", new BigDecimal("300000.00"), 2L}
                ));
        when(reportRepository.topSellingProducts(any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(
                        new Object[]{1L, "Trà sữa trân châu", "M", 15L, new BigDecimal("600000.00")},
                        new Object[]{2L, "Trà đào cam sả", "L", 10L, new BigDecimal("450000.00")}
                ));

        ReportSummaryResponse result = reportService.getSummary(from, to, 5);

        assertNotNull(result);
        assertEquals(from, result.tuNgay());
        assertEquals(to, result.denNgay());
        assertEquals(new BigDecimal("1500000.00"), result.tongDoanhThu());
        assertEquals(10L, result.tongSoDonHang());
        assertEquals(25L, result.tongSoLyDaBan());

        assertEquals(2, result.doanhThuTheoKenh().size());
        assertEquals("TAI_QUAY_POS", result.doanhThuTheoKenh().get(0).maKenh());
        assertEquals(new BigDecimal("1000000.00"), result.doanhThuTheoKenh().get(0).doanhThu());
        assertEquals(6L, result.doanhThuTheoKenh().get(0).soDon());

        assertEquals("ONLINE_WEB", result.doanhThuTheoKenh().get(1).maKenh());
        assertEquals(new BigDecimal("500000.00"), result.doanhThuTheoKenh().get(1).doanhThu());
        assertEquals(4L, result.doanhThuTheoKenh().get(1).soDon());

        assertEquals(3, result.doanhThuTheoPhuongThuc().size());
        assertEquals("Tiền mặt", result.doanhThuTheoPhuongThuc().get(0).tenPhuongThuc());
        assertEquals("VietQR", result.doanhThuTheoPhuongThuc().get(1).tenPhuongThuc());
        assertEquals("Ví MoMo", result.doanhThuTheoPhuongThuc().get(2).tenPhuongThuc());

        assertEquals(2, result.topSanPhamBanChay().size());
        assertEquals("Trà sữa trân châu", result.topSanPhamBanChay().get(0).tenSanPham());
        assertEquals(15L, result.topSanPhamBanChay().get(0).soLuongDaBan());
    }

    @Test
    @DisplayName("Thành công: Không truyền ngày thì mặc định là hôm nay")
    void getSummary_nullDates_defaultsToToday() {
        when(reportRepository.sumRevenueAndCountOrders(any(), any(), any()))
                .thenReturn(List.of());
        when(reportRepository.sumTotalCupsSold(any(), any(), any()))
                .thenReturn(null);
        when(reportRepository.dailyRevenue(any(), any(), any()))
                .thenReturn(List.of());
        when(reportRepository.revenueByChannel(any(), any(), any()))
                .thenReturn(List.of());
        when(reportRepository.revenueByPaymentMethod(any(), any(), any()))
                .thenReturn(List.of());
        when(reportRepository.topSellingProducts(any(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        ReportSummaryResponse result = reportService.getSummary(null, null, null);

        assertNotNull(result);
        LocalDate expectedToday = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        assertEquals(expectedToday, result.tuNgay());
        assertEquals(expectedToday, result.denNgay());
        assertEquals(BigDecimal.ZERO, result.tongDoanhThu());
        assertEquals(0L, result.tongSoDonHang());
        assertEquals(0L, result.tongSoLyDaBan());
    }

    @Test
    @DisplayName("Lỗi: Ngày bắt đầu lớn hơn ngày kết thúc -> 400 Bad Request")
    void getSummary_startDateAfterEndDate_throwsBadRequest() {
        LocalDate from = LocalDate.of(2026, 10, 10);
        LocalDate to = LocalDate.of(2026, 10, 5);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reportService.getSummary(from, to, 5)
        );

        assertTrue(ex.getStatusCode().is4xxClientError());
        assertTrue(ex.getReason().contains("không được lớn hơn"));
    }

    @Test
    @DisplayName("Thành công: Lấy xu hướng doanh thu theo tháng (Zero-filling các tháng không có đơn)")
    void getRevenueTrend_monthly_zeroFillingSuccess() {
        int year = 2026;
        when(reportRepository.revenueTrend(any(), any(), any(), eq("YYYY-MM")))
                .thenReturn(List.of(
                        new Object[]{"2026-01", new BigDecimal("10000000.00"), 100L, 150L},
                        new Object[]{"2026-03", new BigDecimal("15000000.00"), 120L, 180L}
                ));

        List<RevenueTrendResponse> result =
                reportService.getRevenueTrend(year, ReportInterval.MONTH);

        assertNotNull(result);
        assertEquals(12, result.size());

        assertEquals("2026-01", result.get(0).periodKey());
        assertEquals("Tháng 01/2026", result.get(0).label());
        assertEquals(new BigDecimal("10000000.00"), result.get(0).tongDoanhThu());
        assertEquals(100L, result.get(0).tongSoDon());
        assertEquals(150L, result.get(0).tongSoLy());

        assertEquals("2026-02", result.get(1).periodKey());
        assertEquals("Tháng 02/2026", result.get(1).label());
        assertEquals(BigDecimal.ZERO, result.get(1).tongDoanhThu());
        assertEquals(0L, result.get(1).tongSoDon());
        assertEquals(0L, result.get(1).tongSoLy());

        assertEquals("2026-03", result.get(2).periodKey());
        assertEquals(new BigDecimal("15000000.00"), result.get(2).tongDoanhThu());
    }

    @Test
    @DisplayName("Thành công: Lấy xu hướng doanh thu theo quý")
    void getRevenueTrend_quarterly_success() {
        int year = 2026;
        when(reportRepository.revenueTrend(any(), any(), any(), eq("YYYY-\"Q\"Q")))
                .thenReturn(Collections.singletonList(
                        new Object[]{"2026-Q1", new BigDecimal("50000000.00"), 500L, 750L}
                ));

        List<RevenueTrendResponse> result =
                reportService.getRevenueTrend(year, ReportInterval.QUARTER);

        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("2026-Q1", result.get(0).periodKey());
        assertEquals("Quý 1/2026", result.get(0).label());
        assertEquals(new BigDecimal("50000000.00"), result.get(0).tongDoanhThu());

        assertEquals("2026-Q2", result.get(1).periodKey());
        assertEquals("Quý 2/2026", result.get(1).label());
        assertEquals(BigDecimal.ZERO, result.get(1).tongDoanhThu());
    }

    @Test
    @DisplayName("Thành công: Lấy hiệu suất sản phẩm theo doanh thu và tính tỷ trọng %")
    void getProductPerformance_byRevenue_success() {
        when(reportRepository.sumRevenueAndCountOrders(any(), any(), any()))
                .thenReturn(Collections.singletonList(new Object[]{new BigDecimal("100000000.00"), 1000L}));

        List<Object[]> contentList = Collections.singletonList(
                new Object[]{1L, "Trà Sữa Trân Châu Hoàng Gia", "Trà sữa", "https://img.png", 500L, new BigDecimal("25000000.00")}
        );
        PageImpl<Object[]> mockPage = new PageImpl<>(
                contentList,
                PageRequest.of(0, 10),
                1L
        );

        when(reportRepository.findProductPerformanceByRevenue(any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        when(reportRepository.findVariantBreakdownByProductIds(any(), any(), any(), eq(List.of(1L))))
                .thenReturn(List.of(
                        new Object[]{1L, 101L, "M", 300L, new BigDecimal("13500000.00")},
                        new Object[]{1L, 102L, "L", 200L, new BigDecimal("11500000.00")}
                ));

        PageResponse<ProductPerformanceResponse> response =
                reportService.getProductPerformance(2026, 10, null, null, "REVENUE", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        ProductPerformanceResponse product = response.content().get(0);
        assertEquals(1L, product.sanPhamId());
        assertEquals("Trà Sữa Trân Châu Hoàng Gia", product.tenSanPham());
        assertEquals(500L, product.tongSoLyDaBan());
        assertEquals(new BigDecimal("25000000.00"), product.tongDoanhThu());
        assertEquals(25.0, product.tyTrongDoanhThu());

        assertEquals(2, product.chiTietBienThe().size());
        assertEquals("M", product.chiTietBienThe().get(0).kichCo());
        assertEquals(300L, product.chiTietBienThe().get(0).soLuong());
        assertEquals("L", product.chiTietBienThe().get(1).kichCo());
        assertEquals(200L, product.chiTietBienThe().get(1).soLuong());
    }
}
