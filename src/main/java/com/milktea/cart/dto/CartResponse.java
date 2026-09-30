package com.milktea.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long id, String cartToken, List<CartItemResponse> items, BigDecimal total) {}
