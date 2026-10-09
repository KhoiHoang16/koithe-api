package com.milktea.catalog.controller;

import com.milktea.catalog.dto.*;
import com.milktea.catalog.service.ToppingService;
import com.milktea.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/toppings")
public class ToppingController {
    private final ToppingService service;

    public ToppingController(ToppingService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ToppingResponse>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ToppingResponse> create(@Valid @RequestBody ToppingRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<ToppingResponse> update(@PathVariable Long id, @Valid @RequestBody ToppingRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
