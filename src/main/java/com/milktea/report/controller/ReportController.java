package com.milktea.report.controller;

import com.milktea.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report")
public class ReportController {
    @GetMapping
    @Operation(summary = "Get report summary")
    public ApiResponse<Void> summary() {
        return ApiResponse.success(null);
    }
}
