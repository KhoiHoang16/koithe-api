package com.milktea.catalog.controller;
import com.milktea.catalog.dto.*;import com.milktea.catalog.service.VariantService;import com.milktea.common.response.ApiResponse;import jakarta.validation.Valid;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/variants")
public class VariantController {
    private final VariantService service;public VariantController(VariantService service){this.service=service;}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ApiResponse<VariantResponse> update(@PathVariable Long id,@Valid @RequestBody VariantRequest request){return ApiResponse.success(service.update(id,request));}
    @PatchMapping("/{id}/stock") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ApiResponse<VariantResponse> stock(@PathVariable Long id,@Valid @RequestBody StockRequest request){return ApiResponse.success(service.updateStock(id,request));}
}
