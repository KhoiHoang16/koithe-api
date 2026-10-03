package com.milktea.shift.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.security.UserPrincipal;
import com.milktea.shift.dto.*;
import com.milktea.shift.service.CaLamViecService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "POST /api/shifts/open (thu ngân mở ca, nhập tiền đầu ca)")
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

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "POST /api/shifts/{id}/close (nhập tiền kết ca, hệ thống tính chênh lệch)")
    public ApiResponse<CaLamViecResponse> close(
            @PathVariable Long id,
            @RequestBody CaLamViecRequest r) {
        return ApiResponse.success(service.update(id, r));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "GET /api/shifts/current")
    public ApiResponse<CaLamViecResponse> current(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.id() : null;
        return ApiResponse.success(service.findCurrentActive(userId));
    }
}
