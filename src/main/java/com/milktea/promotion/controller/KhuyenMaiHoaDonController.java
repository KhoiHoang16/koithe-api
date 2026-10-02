package com.milktea.promotion.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.KhuyenMaiHoaDonRequest;
import com.milktea.promotion.dto.KhuyenMaiHoaDonResponse;
import com.milktea.promotion.service.KhuyenMaiHoaDonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoice-promotions")
@Tag(name = "Invoice Promotion", description = "Quản lý khuyến mãi theo hóa đơn")
public class KhuyenMaiHoaDonController {
    private final KhuyenMaiHoaDonService service;

    public KhuyenMaiHoaDonController(KhuyenMaiHoaDonService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lấy danh sách khuyến mãi hóa đơn")
    public ApiResponse<PageResponse<KhuyenMaiHoaDonResponse>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.getAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết khuyến mãi hóa đơn")
    public ApiResponse<KhuyenMaiHoaDonResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo khuyến mãi hóa đơn")
    public ApiResponse<KhuyenMaiHoaDonResponse> create(@Valid @RequestBody KhuyenMaiHoaDonRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật khuyến mãi hóa đơn")
    public ApiResponse<KhuyenMaiHoaDonResponse> update(@PathVariable Long id,
            @Valid @RequestBody KhuyenMaiHoaDonRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa khuyến mãi hóa đơn")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
