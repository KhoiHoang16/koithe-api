package com.milktea.promotion.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.KhuyenMaiVoucherRequest;
import com.milktea.promotion.dto.KhuyenMaiVoucherResponse;
import com.milktea.promotion.service.KhuyenMaiVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
@Tag(name = "Voucher", description = "Quản lý voucher khuyến mãi")
public class KhuyenMaiVoucherController {
    private final KhuyenMaiVoucherService service;

    public KhuyenMaiVoucherController(KhuyenMaiVoucherService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lấy danh sách voucher")
    public ApiResponse<PageResponse<KhuyenMaiVoucherResponse>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.getAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết voucher")
    public ApiResponse<KhuyenMaiVoucherResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo voucher")
    public ApiResponse<KhuyenMaiVoucherResponse> create(@Valid @RequestBody KhuyenMaiVoucherRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật voucher")
    public ApiResponse<KhuyenMaiVoucherResponse> update(@PathVariable Long id,
            @Valid @RequestBody KhuyenMaiVoucherRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa voucher")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
