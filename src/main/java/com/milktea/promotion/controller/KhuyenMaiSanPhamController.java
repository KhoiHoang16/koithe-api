package com.milktea.promotion.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.KhuyenMaiSanPhamRequest;
import com.milktea.promotion.dto.KhuyenMaiSanPhamResponse;
import com.milktea.promotion.service.KhuyenMaiSanPhamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-promotions")
@Tag(name = "Product Promotion", description = "Quản lý khuyến mãi theo sản phẩm hoặc danh mục")
@SecurityRequirement(name = "bearerAuth")
public class KhuyenMaiSanPhamController {
    private final KhuyenMaiSanPhamService service;

    public KhuyenMaiSanPhamController(KhuyenMaiSanPhamService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách khuyến mãi sản phẩm")
    public ApiResponse<PageResponse<KhuyenMaiSanPhamResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.getAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết khuyến mãi sản phẩm")
    public ApiResponse<KhuyenMaiSanPhamResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo khuyến mãi sản phẩm")
    public ApiResponse<KhuyenMaiSanPhamResponse> create(@Valid @RequestBody KhuyenMaiSanPhamRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật khuyến mãi sản phẩm")
    public ApiResponse<KhuyenMaiSanPhamResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody KhuyenMaiSanPhamRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa khuyến mãi sản phẩm")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
