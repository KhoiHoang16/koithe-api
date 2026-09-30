package com.milktea.cart.dto;

import java.math.BigDecimal;

public record CartToppingResponse(Long id, Long toppingId, String name, Integer quantity, BigDecimal unitPrice) {}
