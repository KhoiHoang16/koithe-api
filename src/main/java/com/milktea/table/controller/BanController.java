package com.milktea.table.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.table.dto.*;
import com.milktea.table.service.BanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "Table", description = "Quản lý bàn và mã QR bàn")
public class BanController {
    private final BanService service;

    public BanController(BanService s) {
        service = s;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "GET /api/tables - Danh sách tất cả bàn")
    public ApiResponse<List<BanResponse>> list() {
        return ApiResponse.success(service.findAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "POST /api/tables - Tạo bàn mới")
    public ApiResponse<BanResponse> create(@RequestBody BanRequest r) {
        return ApiResponse.success(service.create(r));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "GET /api/tables/{id} - Chi tiết bàn")
    public ApiResponse<BanResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "PUT /api/tables/{id} - Cập nhật thông tin bàn")
    public ApiResponse<BanResponse> update(@PathVariable Long id, @RequestBody BanRequest r) {
        return ApiResponse.success(service.update(id, r));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "PATCH /api/tables/{id}/status - Đổi trạng thái bàn (TRONG, DANG_CO_KHACH, DA_DAT_TRUOC)")
    public ApiResponse<BanResponse> updateStatus(@PathVariable Long id, @RequestBody BanRequest r) {
        return ApiResponse.success(service.updateStatus(id, r.trangThai()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "DELETE /api/tables/{id} - Xóa bàn")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/qr/{token}")
    @Operation(summary = "GET /api/tables/qr/{token} - Public, cho khách quét QR xem thông tin bàn")
    public ApiResponse<BanResponse> getByQrToken(@PathVariable String token) {
        return ApiResponse.success(service.findByQrToken(token));
    }
}
