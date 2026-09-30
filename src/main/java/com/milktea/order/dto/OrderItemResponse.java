package com.milktea.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemResponse(Long id, Long variantId, String productName, String size, Integer quantity,
        BigDecimal unitPrice, BigDecimal lineTotal, String sugarLevel, String iceLevel, String note,
        List<OrderToppingResponse> toppings) {}
