package com.milktea.inventory.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.inventory.dto.ChiTietPhieuNhapRequest;
import com.milktea.inventory.dto.ChiTietPhieuNhapResponse;
import com.milktea.inventory.service.ChiTietPhieuNhapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-order-lines")
@Tag(name = "Purchase Order Line", description = "Quản lý dòng chi tiết phiếu nhập hàng")
public class ChiTietPhieuNhapController {
    private final ChiTietPhieuNhapService service;

    public ChiTietPhieuNhapController(ChiTietPhieuNhapService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "GET /api/purchase-order-lines - Lấy danh sách dòng chi tiết theo mã phiếu nhập")
    public ApiResponse<PageResponse<ChiTietPhieuNhapResponse>> getAllByMaPhieuNhap(
            @RequestParam Long maPhieuNhap,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.getAllByMaPhieuNhap(maPhieuNhap, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/purchase-order-lines/{id} - Lấy chi tiết một dòng phiếu nhập")
    public ApiResponse<ChiTietPhieuNhapResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "POST /api/purchase-order-lines - Thêm dòng vào phiếu nhập chưa duyệt")
    public ApiResponse<ChiTietPhieuNhapResponse> create(@Valid @RequestBody ChiTietPhieuNhapRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "PUT /api/purchase-order-lines/{id} - Cập nhật dòng của phiếu nhập chưa duyệt")
    public ApiResponse<ChiTietPhieuNhapResponse> update(@PathVariable Long id,
            @Valid @RequestBody ChiTietPhieuNhapRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "DELETE /api/purchase-order-lines/{id} - Xóa dòng khỏi phiếu nhập chưa duyệt")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
