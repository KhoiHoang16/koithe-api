package com.milktea.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrderStatusRequest(@NotBlank @Pattern(regexp = "CHO_XAC_NHAN|DA_THANH_TOAN|DANG_PHA_CHE|SAN_SANG|HOAN_THANH|DA_HUY") String status) {}
