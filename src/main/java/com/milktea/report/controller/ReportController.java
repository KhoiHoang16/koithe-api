package com.milktea.report.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.report.dto.*;
import com.milktea.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/revenue-trend")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Xem biểu đồ xu hướng doanh thu gom nhóm theo Tháng / Quý / Năm")
    public ApiResponse<List<RevenueTrendResponse>> revenueTrend(
            @Parameter(description = "Năm cần xem (ví dụ: 2026, mặc định: năm hiện tại)")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Gom nhóm theo: MONTH | QUARTER | YEAR, mặc định: MONTH")
            @RequestParam(defaultValue = "MONTH") ReportInterval groupBy) {
        return ApiResponse.success(service.getRevenueTrend(year, groupBy));
    }

    @GetMapping("/product-performance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Báo cáo doanh thu và hiệu suất bán hàng của từng sản phẩm trong Tháng / Quý / Năm")
    public ApiResponse<PageResponse<ProductPerformanceResponse>> productPerformance(
            @Parameter(description = "Năm cần xem (ví dụ: 2026, mặc định: năm hiện tại)")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Tháng cần xem (1 - 12)")
            @RequestParam(required = false) Integer month,

            @Parameter(description = "Quý cần xem (1 - 4)")
            @RequestParam(required = false) Integer quarter,

            @Parameter(description = "Lọc theo ID danh mục sản phẩm")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Sắp xếp theo: REVENUE (Doanh thu) | QUANTITY (Số ly bán), mặc định: REVENUE")
            @RequestParam(defaultValue = "REVENUE") String sortBy,

            @Parameter(description = "Trang hiện tại (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng bản ghi mỗi trang")
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(service.getProductPerformance(year, month, quarter, categoryId, sortBy, page, size));
    }
}
