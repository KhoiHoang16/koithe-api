package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiRequest;
import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChuongTrinhKhuyenMaiMapper {
    ChuongTrinhKhuyenMaiResponse toResponse(ChuongTrinhKhuyenMai e);

    @Mapping(target = "id", ignore = true)
    ChuongTrinhKhuyenMai toEntity(ChuongTrinhKhuyenMaiRequest r);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget ChuongTrinhKhuyenMai entity, ChuongTrinhKhuyenMaiRequest r);
}
