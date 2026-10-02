package com.milktea.inventory.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.inventory.dto.NhaCungCapRequest;
import com.milktea.inventory.dto.NhaCungCapResponse;
import com.milktea.inventory.service.NhaCungCapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Supplier", description = "Quản lý nhà cung cấp")
public class NhaCungCapController {
    private final NhaCungCapService service;

    public NhaCungCapController(NhaCungCapService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lấy danh sách nhà cung cấp")
    public ApiResponse<PageResponse<NhaCungCapResponse>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.getAll(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết nhà cung cấp")
    public ApiResponse<NhaCungCapResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo nhà cung cấp")
    public ApiResponse<NhaCungCapResponse> create(@Valid @RequestBody NhaCungCapRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật nhà cung cấp")
    public ApiResponse<NhaCungCapResponse> update(@PathVariable Long id,
            @Valid @RequestBody NhaCungCapRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa nhà cung cấp")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
