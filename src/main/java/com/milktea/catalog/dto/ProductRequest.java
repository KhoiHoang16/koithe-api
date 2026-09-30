package com.milktea.catalog.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record ProductRequest(@NotNull Long maDanhMuc, @NotBlank @Size(max = 255) String tenSanPham, String moTa) {}
