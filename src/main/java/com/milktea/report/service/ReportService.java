package com.milktea.report.service;

import com.milktea.common.response.PageResponse;
import com.milktea.report.dto.*;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    ReportSummaryResponse getSummary(LocalDate from, LocalDate to, Integer top);

    List<RevenueTrendResponse> getRevenueTrend(
        Integer year,
        ReportInterval interval
    );

    PageResponse<ProductPerformanceResponse> getProductPerformance(
        Integer year,
        Integer month,
        Integer quarter,
        Long categoryId,
        String sortBy,
        int page,
        int size
    );
}
