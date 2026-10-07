package com.milktea.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KhachHangUpdateRequest(
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không quá 100 ký tự")
    @Schema(example = "Nguyễn Văn A")
    String hoVaTen,

    @Email(message = "Email không hợp lệ")
    @Schema(example = "nguyenvana@gmail.com")
    String email
) {}
