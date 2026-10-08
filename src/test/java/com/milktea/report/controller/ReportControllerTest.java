package com.milktea.report.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.milktea.report.dto.*;
import com.milktea.report.service.ReportService;
import com.milktea.security.JwtFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ReportService reportService;

    @MockBean
    JwtFilter jwtFilter;

    @Test
    @DisplayName("GET /api/reports - Trả về cấu trúc JSON báo cáo thành công")
    void summary_returnsReportData() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 8);

        ReportSummaryResponse mockResponse = new ReportSummaryResponse(
                from,
                to,
                new BigDecimal("2500000.00"),
                30L,
                45L,
                List.of(new DoanhThuTheoNgayResponse(from, new BigDecimal("500000.00"), 5L)),
                List.of(
                        new DoanhThuKenhResponse("TAI_QUAY_POS", "Tại quầy POS", new BigDecimal("1800000.00"), 20L),
                        new DoanhThuKenhResponse("ONLINE_WEB", "Website / Quét QR bàn", new BigDecimal("700000.00"), 10L)
                ),
                List.of(
                        new DoanhThuPhuongThucResponse("TIEN_MAT", "Tiền mặt", new BigDecimal("1000000.00"), 12L),
                        new DoanhThuPhuongThucResponse("CHUYEN_KHOAN_QR", "VietQR", new BigDecimal("1500000.00"), 18L)
                ),
                List.of(new TopSanPhamResponse(1L, "Trà sữa trân châu", "M", 25L, new BigDecimal("1000000.00")))
        );

        when(reportService.getSummary(eq(from), eq(to), eq(5))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/reports")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-08")
                        .param("top", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tuNgay").value("2026-10-01"))
                .andExpect(jsonPath("$.data.denNgay").value("2026-10-08"))
                .andExpect(jsonPath("$.data.tongDoanhThu").value(2500000.00))
                .andExpect(jsonPath("$.data.tongSoDonHang").value(30))
                .andExpect(jsonPath("$.data.tongSoLyDaBan").value(45))
                .andExpect(jsonPath("$.data.doanhThuTheoKenh[0].maKenh").value("TAI_QUAY_POS"))
                .andExpect(jsonPath("$.data.doanhThuTheoKenh[1].maKenh").value("ONLINE_WEB"))
                .andExpect(jsonPath("$.data.doanhThuTheoPhuongThuc[0].tenPhuongThuc").value("Tiền mặt"))
                .andExpect(jsonPath("$.data.topSanPhamBanChay[0].tenSanPham").value("Trà sữa trân châu"));

        verify(reportService).getSummary(eq(from), eq(to), eq(5));
    }
}
