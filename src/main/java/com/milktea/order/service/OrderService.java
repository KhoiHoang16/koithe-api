package com.milktea.order.service;

import com.milktea.common.response.PageResponse;
import com.milktea.order.dto.*;
import com.milktea.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse create(CreateOrderRequest request, UserPrincipal principal);
    OrderResponse createPos(CreatePosOrderRequest request);
    OrderResponse get(Long id, UserPrincipal principal);
    PageResponse<OrderResponse> list(String status, String channel, Pageable pageable);
    PageResponse<OrderResponse> myOrders(UserPrincipal principal, Pageable pageable);
    OrderResponse updateStatus(Long id, String status);
    OrderResponse cancel(Long id, UserPrincipal principal);
}
