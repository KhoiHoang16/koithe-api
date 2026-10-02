package com.milktea.shift.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.shift.dto.*;
import com.milktea.shift.service.CaLamViecService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Shift")
public class CaLamViecController {
    private final CaLamViecService service;

    public CaLamViecController(CaLamViecService s) {
        service = s;
    }

    @GetMapping
    @Operation(summary = "List shifts")
    public ApiResponse<List<CaLamViecResponse>> list() {
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shift")
    public ApiResponse<CaLamViecResponse> get(@PathVariable Long id) {
        return ApiResponse.success(null);
    }

    @PostMapping
    @Operation(summary = "Create shift")
    public ApiResponse<CaLamViecResponse> create(@RequestBody CaLamViecRequest r) {
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update shift")
    public ApiResponse<CaLamViecResponse> update(@PathVariable Long id, @RequestBody CaLamViecRequest r) {
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete shift")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return ApiResponse.success(null);
    }
}
