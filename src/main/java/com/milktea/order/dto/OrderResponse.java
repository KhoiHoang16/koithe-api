package com.milktea.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String displayCode, String status, String channel, String serviceType,
        Long customerId, Long cashierId, Long shiftId, Long tableId, String tenNguoiNhan,
        String soDienThoaiNguoiNhan, String diaChiGiaoHang, BigDecimal merchandiseTotal,
        BigDecimal discount, BigDecimal payableTotal, Instant createdAt, List<OrderItemResponse> items) {}
