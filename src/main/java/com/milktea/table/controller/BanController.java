package com.milktea.table.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.table.dto.*;
import com.milktea.table.service.BanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "GET /api/tables - Danh sách bàn")
    public ApiResponse<List<BanResponse>> list() {
        return ApiResponse.success(service.findAll());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "PATCH /api/tables/{id}/status - Đổi trạng thái bàn")
    public ApiResponse<BanResponse> updateStatus(@PathVariable Long id, @RequestBody BanRequest r) {
        return ApiResponse.success(service.updateStatus(id, r.trangThai()));
    }

    @GetMapping("/qr/{token}")
    @Operation(summary = "GET /api/tables/qr/{token} - Public, cho khách quét QR")
    public ApiResponse<BanResponse> getByQrToken(@PathVariable String token) {
        return ApiResponse.success(service.findByQrToken(token));
    }
}
