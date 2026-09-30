package com.milktea.catalog.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
public record ToppingRequest(@NotBlank @Size(max = 255) String tenTopping, @NotNull @DecimalMin(value = "0.01") BigDecimal giaBan, @DecimalMin(value = "0.00") BigDecimal soLuongTon) {}
