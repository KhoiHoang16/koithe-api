package com.milktea.shift.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.security.UserPrincipal;
import com.milktea.shift.dto.*;
import com.milktea.shift.service.CaLamViecService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Shift")
public class CaLamViecController {
    private final CaLamViecService service;

    public CaLamViecController(CaLamViecService s) {
        service = s;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "GET /api/shifts - Danh sách ca làm việc")
    public ApiResponse<List<CaLamViecResponse>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(service.findAll(status));
    }

    @GetMapping({"/current", "/active"})
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "GET /api/shifts/current - Lấy ca đang mở hiện tại")
    public ApiResponse<CaLamViecResponse> current(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.id() : null;
        return ApiResponse.success(service.findCurrentActive(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "GET /api/shifts/{id} - Chi tiết ca làm việc")
    public ApiResponse<CaLamViecResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping({"/open", ""})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "POST /api/shifts/open - Thu ngân mở ca, nhập tiền đầu ca")
    public ApiResponse<CaLamViecResponse> open(
            @RequestBody CaLamViecRequest r,
            @AuthenticationPrincipal UserPrincipal principal) {
        CaLamViecRequest request = r;
        if (request.maThuNgan() == null && principal != null) {
            request = new CaLamViecRequest(
                    principal.id(),
                    r.thoiGianBatDau(),
                    r.thoiGianKetThuc(),
                    r.tienDauCa(),
                    r.tienKetCa(),
                    r.trangThai());
        }
        return ApiResponse.success(service.create(request));
    }

    @PostMapping({"/{id}/close", "/close"})
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "POST /api/shifts/{id}/close - Nhập tiền kết ca, hệ thống tính chênh lệch")
    public ApiResponse<CaLamViecResponse> close(
            @PathVariable(required = false) Long id,
            @RequestBody CaLamViecRequest r,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long targetId = id;
        if (targetId == null) {
            Long userId = principal != null ? principal.id() : null;
            CaLamViecResponse current = service.findCurrentActive(userId);
            if (current != null) {
                targetId = current.id();
            }
        }
        return ApiResponse.success(service.update(targetId, r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "PUT /api/shifts/{id} - Cập nhật ca làm việc")
    public ApiResponse<CaLamViecResponse> update(@PathVariable Long id, @RequestBody CaLamViecRequest r) {
        return ApiResponse.success(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "DELETE /api/shifts/{id} - Xóa ca làm việc")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
