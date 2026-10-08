package com.milktea.promotion.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiRequest;
import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiResponse;
import com.milktea.promotion.service.ChuongTrinhKhuyenMaiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotion-programs")
@Tag(name = "Promotion Program", description = "Quản lý chương trình khuyến mãi")
@SecurityRequirement(name = "bearerAuth")
public class ChuongTrinhKhuyenMaiController {
    private final ChuongTrinhKhuyenMaiService service;

    public ChuongTrinhKhuyenMaiController(ChuongTrinhKhuyenMaiService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách chương trình khuyến mãi")
    public ApiResponse<PageResponse<ChuongTrinhKhuyenMaiResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.getAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết chương trình khuyến mãi")
    public ApiResponse<ChuongTrinhKhuyenMaiResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo chương trình khuyến mãi")
    public ApiResponse<ChuongTrinhKhuyenMaiResponse> create(@Valid @RequestBody ChuongTrinhKhuyenMaiRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật chương trình khuyến mãi")
    public ApiResponse<ChuongTrinhKhuyenMaiResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ChuongTrinhKhuyenMaiRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa chương trình khuyến mãi")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
