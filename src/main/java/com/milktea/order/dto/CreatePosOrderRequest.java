package com.milktea.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CreatePosOrderRequest(@NotNull @Positive Long cashierId, @NotNull @Positive Long shiftId,
        @NotEmpty @Valid List<PosOrderItemRequest> items,
        @Pattern(regexp = "TAI_CHO|MANG_VE|GIAO_HANG") String serviceType,
        @Positive Long tableId, @Positive Long customerId) {}
