package com.milktea.catalog.dto;
import java.time.Instant;
public record CategoryResponse(Long id, String tenDanhMuc, Instant createdAt, Instant updatedAt) {}
