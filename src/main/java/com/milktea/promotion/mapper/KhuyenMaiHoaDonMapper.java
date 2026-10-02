package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface KhuyenMaiHoaDonMapper {
    KhuyenMaiHoaDonResponse toResponse(KhuyenMaiHoaDon e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    KhuyenMaiHoaDon toEntity(KhuyenMaiHoaDonRequest r);
}
