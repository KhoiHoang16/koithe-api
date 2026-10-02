package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface KhuyenMaiVoucherMapper {
    KhuyenMaiVoucherResponse toResponse(KhuyenMaiVoucher e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    KhuyenMaiVoucher toEntity(KhuyenMaiVoucherRequest r);
}
