package com.milktea.inventory.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.inventory.dto.PhieuNhapHangRequest;
import com.milktea.inventory.dto.PhieuNhapHangResponse;
import com.milktea.inventory.service.PhieuNhapHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@Tag(name = "Purchase Order", description = "Quản lý phiếu nhập hàng")
public class PhieuNhapHangController {
    private final PhieuNhapHangService service;

    public PhieuNhapHangController(PhieuNhapHangService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lấy danh sách phiếu nhập hàng theo bộ lọc")
    public ApiResponse<PageResponse<PhieuNhapHangResponse>> getAll(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.getAll(supplierId, fromDate, toDate, trangThai, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết phiếu nhập hàng")
    public ApiResponse<PhieuNhapHangResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo phiếu nhập hàng kèm danh sách mặt hàng")
    public ApiResponse<PhieuNhapHangResponse> create(@Valid @RequestBody PhieuNhapHangRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật phiếu nhập hàng")
    public ApiResponse<PhieuNhapHangResponse> update(@PathVariable Long id,
            @Valid @RequestBody PhieuNhapHangRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Duyệt phiếu nhập và ghi nhận nhập kho")
    public ApiResponse<PhieuNhapHangResponse> approve(@PathVariable Long id) {
        return ApiResponse.success(service.approve(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Hủy phiếu nhập chưa được duyệt")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
