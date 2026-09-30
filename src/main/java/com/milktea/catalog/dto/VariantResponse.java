package com.milktea.catalog.dto;
import java.math.BigDecimal;
import java.time.Instant;
public record VariantResponse(Long id, Long maSanPham, String kichCo, BigDecimal giaBan, BigDecimal soLuongTon, boolean conHang, Instant createdAt, Instant updatedAt) {}
