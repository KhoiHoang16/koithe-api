package com.milktea.report.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.milktea.report.dto.ReportSummaryResponse;
import com.milktea.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
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
                .thenReturn(java.util.Collections.singletonList(new Object[]{new BigDecimal("1500000.00"), 10L}));
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

        // Kiểm tra phân bổ kênh bán hàng (TAI_QUAY_POS vs ONLINE_WEB)
        assertEquals(2, result.doanhThuTheoKenh().size());
        assertEquals("TAI_QUAY_POS", result.doanhThuTheoKenh().get(0).maKenh());
        assertEquals(new BigDecimal("1000000.00"), result.doanhThuTheoKenh().get(0).doanhThu());
        assertEquals(6L, result.doanhThuTheoKenh().get(0).soDon());

        assertEquals("ONLINE_WEB", result.doanhThuTheoKenh().get(1).maKenh());
        assertEquals(new BigDecimal("500000.00"), result.doanhThuTheoKenh().get(1).doanhThu()); // 300k + 200k
        assertEquals(4L, result.doanhThuTheoKenh().get(1).soDon()); // 2 + 2

        // Kiểm tra phân bổ thanh toán
        assertEquals(3, result.doanhThuTheoPhuongThuc().size());
        assertEquals("Tiền mặt", result.doanhThuTheoPhuongThuc().get(0).tenPhuongThuc());
        assertEquals("VietQR", result.doanhThuTheoPhuongThuc().get(1).tenPhuongThuc());
        assertEquals("Ví MoMo", result.doanhThuTheoPhuongThuc().get(2).tenPhuongThuc());

        // Kiểm tra top sản phẩm
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
        assertEquals(LocalDate.now(), result.tuNgay());
        assertEquals(LocalDate.now(), result.denNgay());
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
}
