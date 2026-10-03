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
import com.milktea.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Operation(summary = "GET /api/purchase-orders - Danh sách phiếu nhập hàng (lọc theo from, to, supplierId)")
    public ApiResponse<PageResponse<PhieuNhapHangResponse>> getAll(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Instant effectiveFrom = from != null ? from : fromDate;
        Instant effectiveTo = to != null ? to : toDate;
        return ApiResponse.success(service.getAll(supplierId, effectiveFrom, effectiveTo, trangThai, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/purchase-orders/{id} - Lấy chi tiết phiếu nhập hàng")
    public ApiResponse<PhieuNhapHangResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "POST /api/purchase-orders - Tạo phiếu nhập hàng kèm danh sách mặt hàng")
    public ApiResponse<PhieuNhapHangResponse> create(
            @Valid @RequestBody PhieuNhapHangRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PhieuNhapHangRequest req = request;
        if (req.maNguoiNhap() == null && principal != null) {
            req = new PhieuNhapHangRequest(
                    req.maPhieuNhap(),
                    req.maNhaCungCap(),
                    principal.id(),
                    req.tongTien(),
                    req.ghiChu(),
                    req.trangThai(),
                    req.ngayNhap(),
                    req.chiTiet());
        }
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "PUT /api/purchase-orders/{id} - Cập nhật phiếu nhập hàng")
    public ApiResponse<PhieuNhapHangResponse> update(@PathVariable Long id,
            @Valid @RequestBody PhieuNhapHangRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @RequestMapping(value = "/{id}/approve", method = {RequestMethod.POST, RequestMethod.PATCH})
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "POST /api/purchase-orders/{id}/approve - Duyệt phiếu nhập và cập nhật tồn kho (hỗ trợ cả PATCH)")
    public ApiResponse<PhieuNhapHangResponse> approve(@PathVariable Long id) {
        return ApiResponse.success(service.approve(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "DELETE /api/purchase-orders/{id} - Hủy phiếu nhập chưa được duyệt")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
