package com.milktea.table.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.table.dto.*;
import com.milktea.table.service.BanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "Table")
public class BanController {
    private final BanService service;

    public BanController(BanService s) {
        service = s;
    }

    @GetMapping
    @Operation(summary = "List tables")
    public ApiResponse<List<BanResponse>> list() {
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get table")
    public ApiResponse<BanResponse> get(@PathVariable Long id) {
        return ApiResponse.success(null);
    }

    @PostMapping
    @Operation(summary = "Create table")
    public ApiResponse<BanResponse> create(@RequestBody BanRequest r) {
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update table")
    public ApiResponse<BanResponse> update(@PathVariable Long id, @RequestBody BanRequest r) {
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete table")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return ApiResponse.success(null);
    }
}
