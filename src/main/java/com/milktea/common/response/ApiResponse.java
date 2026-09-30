package com.milktea.common.response;

import java.time.Instant;

public record ApiResponse<T>(int code, String message, T data, Instant timestamp) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data, Instant.now());
    }
}
