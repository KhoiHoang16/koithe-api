package com.milktea.catalog.dto;
import java.time.Instant;
public record ProductResponse(Long id, Long maDanhMuc, String tenDanhMuc, String tenSanPham, String moTa, Instant createdAt, Instant updatedAt) {}
