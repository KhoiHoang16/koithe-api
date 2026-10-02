package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ChuongTrinhKhuyenMaiMapper {
    ChuongTrinhKhuyenMaiResponse toResponse(ChuongTrinhKhuyenMai e);

    @Mapping(target = "id", ignore = true)
    ChuongTrinhKhuyenMai toEntity(ChuongTrinhKhuyenMaiRequest r);
}
