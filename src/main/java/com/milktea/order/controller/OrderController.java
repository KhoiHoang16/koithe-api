package com.milktea.order.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.common.response.PageResponse;
import com.milktea.order.dto.*;
import com.milktea.order.service.OrderService;
import com.milktea.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) { return ApiResponse.success(service.create(request, principal)); }

    @PostMapping("/pos") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    public ApiResponse<OrderResponse> pos(@Valid @RequestBody CreatePosOrderRequest request) {
        return ApiResponse.success(service.createPos(request));
    }

    @GetMapping("/my") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<PageResponse<OrderResponse>> mine(@AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(service.myOrders(principal, pageable));
    }

    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    public ApiResponse<PageResponse<OrderResponse>> list(@RequestParam(required = false) String status,
            @RequestParam(required = false) String channel,
            @PageableDefault(size = 20, sort = "ngayTao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(service.list(status, channel, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.get(id, principal));
    }

    @PatchMapping("/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER','BARISTA')")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResponse.success(service.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.cancel(id, principal));
    }
}
