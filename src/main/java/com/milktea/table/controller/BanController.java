package com.milktea.table.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.table.dto.*;
import com.milktea.table.service.BanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "Table")
public class BanController {
    private final BanService service;

    public BanController(BanService s) {
        service = s;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "List tables")
    public ApiResponse<List<BanResponse>> list() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "Get table")
    public ApiResponse<BanResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create table")
    public ApiResponse<BanResponse> create(@RequestBody BanRequest r) {
        return ApiResponse.success(service.create(r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "Update table")
    public ApiResponse<BanResponse> update(@PathVariable Long id, @RequestBody BanRequest r) {
        return ApiResponse.success(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Delete table")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
