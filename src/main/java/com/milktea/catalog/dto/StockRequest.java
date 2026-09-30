package com.milktea.catalog.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record StockRequest(@NotNull @DecimalMin(value = "0.00") BigDecimal soLuongTon) {}
