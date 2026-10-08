package com.milktea.report.service;

import com.milktea.report.dto.ReportSummaryResponse;
import java.time.LocalDate;

public interface ReportService {
    ReportSummaryResponse getSummary(LocalDate from, LocalDate to, Integer top);
}
