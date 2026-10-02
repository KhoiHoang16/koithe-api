package com.milktea.inventory.mapper;

import com.milktea.inventory.dto.*;
import com.milktea.inventory.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PhieuNhapHangMapper {
    PhieuNhapHangResponse toResponse(PhieuNhapHang e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nhaCungCap", ignore = true)
    @Mapping(target = "nguoiNhap", ignore = true)
    PhieuNhapHang toEntity(PhieuNhapHangRequest r);
}
