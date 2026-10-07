package com.milktea.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record KhachHangCreateRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    @Schema(example = "0901234567")
    String soDienThoai,

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không quá 100 ký tự")
    @Schema(example = "Nguyễn Văn A")
    String hoVaTen,

    @Email(message = "Email không hợp lệ")
    @Schema(example = "nguyenvana@gmail.com")
    String email,

    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    @Schema(example = "123456")
    String matKhau
) {}
