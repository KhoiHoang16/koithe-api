package com.milktea.cart.dto;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(@Min(1) Integer quantity, String sugarLevel, String iceLevel) {}
