package com.milktea.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderToppingRequest(@NotNull Long toppingId, @NotNull @Min(1) Integer quantity) {}
