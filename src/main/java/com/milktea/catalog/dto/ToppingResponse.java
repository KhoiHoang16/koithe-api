package com.milktea.catalog.dto;
import java.math.BigDecimal;
import java.time.Instant;
public record ToppingResponse(Long id, String tenTopping, BigDecimal giaBan, BigDecimal soLuongTon, boolean conHang, Instant createdAt, Instant updatedAt) {}
