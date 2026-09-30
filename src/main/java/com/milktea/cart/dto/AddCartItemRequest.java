package com.milktea.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddCartItemRequest(@NotNull Long variantId, @NotNull @Min(1) Integer quantity,
        String sugarLevel, String iceLevel, String note,
        @Valid List<@NotNull CartToppingRequest> toppings) {}
