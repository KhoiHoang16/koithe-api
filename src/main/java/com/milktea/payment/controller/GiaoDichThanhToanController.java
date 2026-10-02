package com.milktea.payment.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.payment.dto.*;
import com.milktea.payment.service.GiaoDichThanhToanService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment")
public class GiaoDichThanhToanController {
    private final GiaoDichThanhToanService service;

    public GiaoDichThanhToanController(GiaoDichThanhToanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List payments")
    public ApiResponse<List<GiaoDichThanhToanResponse>> list() {
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment")
    public ApiResponse<GiaoDichThanhToanResponse> get(@PathVariable Long id) {
        return ApiResponse.success(null);
    }

    @PostMapping
    @Operation(summary = "Create payment")
    public ApiResponse<GiaoDichThanhToanResponse> create(@RequestBody GiaoDichThanhToanRequest request) {
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment")
    public ApiResponse<GiaoDichThanhToanResponse> update(@PathVariable Long id,
            @RequestBody GiaoDichThanhToanRequest request) {
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return ApiResponse.success(null);
    }
}
