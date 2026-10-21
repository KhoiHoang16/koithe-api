package com.milktea.catalog.dto;
import java.math.BigDecimal;
import java.time.Instant;
public record ProductResponse(Long id, Long maDanhMuc, String tenDanhMuc, String tenSanPham, String moTa,
        String hinhAnh, boolean noiBat, BigDecimal giaTu, Instant createdAt, Instant updatedAt) {}
