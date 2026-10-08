package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.KhuyenMaiHoaDonRequest;
import com.milktea.promotion.dto.KhuyenMaiHoaDonResponse;
import com.milktea.promotion.entity.KhuyenMaiHoaDon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface KhuyenMaiHoaDonMapper {
    @Mapping(target = "maChuongTrinh", source = "chuongTrinh.id")
    KhuyenMaiHoaDonResponse toResponse(KhuyenMaiHoaDon e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    KhuyenMaiHoaDon toEntity(KhuyenMaiHoaDonRequest r);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    void updateEntity(@MappingTarget KhuyenMaiHoaDon entity, KhuyenMaiHoaDonRequest r);
}
