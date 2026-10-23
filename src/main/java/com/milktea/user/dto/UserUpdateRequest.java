package com.milktea.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
    @NotBlank(message = "Họ và tên không được để trống")
    String fullName,

    String phone,

    String role,

    Boolean isActive,

    String password
) {}
