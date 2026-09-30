package com.milktea.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartToppingRequest(@NotNull Long toppingId, @NotNull @Min(1) Integer quantity) {}
