package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.KhuyenMaiVoucherRequest;
import com.milktea.promotion.dto.KhuyenMaiVoucherResponse;
import com.milktea.promotion.entity.KhuyenMaiVoucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface KhuyenMaiVoucherMapper {
    @Mapping(target = "maChuongTrinh", source = "chuongTrinh.id")
    KhuyenMaiVoucherResponse toResponse(KhuyenMaiVoucher e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    KhuyenMaiVoucher toEntity(KhuyenMaiVoucherRequest r);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    void updateEntity(@MappingTarget KhuyenMaiVoucher entity, KhuyenMaiVoucherRequest r);
}
