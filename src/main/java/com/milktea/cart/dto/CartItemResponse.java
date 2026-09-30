package com.milktea.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartItemResponse(Long id, Long variantId, String productName, String size,
        Integer quantity, String sugarLevel, String iceLevel, String note,
        List<CartToppingResponse> toppings, BigDecimal unitPrice, BigDecimal lineTotal) {}
