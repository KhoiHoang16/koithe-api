package com.milktea.order.dto;

import java.math.BigDecimal;

public record OrderToppingResponse(Long id, Long toppingId, String name, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
