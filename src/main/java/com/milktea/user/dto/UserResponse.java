package com.milktea.user.dto;

import java.time.Instant;

public record UserResponse(
    Long id,
    String username,
    String fullName,
    String phone,
    String role,
    Long roleId,
    boolean isActive,
    Instant createdAt
) {}
