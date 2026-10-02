package com.milktea.customer.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.customer.dto.*;
import com.milktea.customer.service.KhachHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer")
public class KhachHangController {
    private final KhachHangService service;

    public KhachHangController(KhachHangService s) {
        service = s;
    }

    @GetMapping
    @Operation(summary = "List customers")
    public ApiResponse<List<KhachHangResponse>> list() {
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer")
    public ApiResponse<KhachHangResponse> get(@PathVariable Long id) {
        return ApiResponse.success(null);
    }

    @PostMapping
    @Operation(summary = "Create customer")
    public ApiResponse<KhachHangResponse> create(@RequestBody KhachHangRequest r) {
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ApiResponse<KhachHangResponse> update(@PathVariable Long id, @RequestBody KhachHangRequest r) {
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        return ApiResponse.success(null);
    }
}
