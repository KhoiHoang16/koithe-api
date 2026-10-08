package com.milktea.report.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.report.dto.ReportSummaryResponse;
import com.milktea.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report", description = "Báo cáo thống kê doanh thu và đơn hàng")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Xem tổng hợp báo cáo kinh doanh (Doanh thu, Đơn hàng, Số ly, Kênh bán, Top món)")
    public ApiResponse<ReportSummaryResponse> summary(
            @Parameter(description = "Từ ngày (YYYY-MM-DD), mặc định: hôm nay")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Đến ngày (YYYY-MM-DD), mặc định: hôm nay")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Số lượng món bán chạy muốn lấy, mặc định: 5")
            @RequestParam(defaultValue = "5") Integer top) {
        return ApiResponse.success(service.getSummary(from, to, top));
    }
}
