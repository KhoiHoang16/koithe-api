package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface KhuyenMaiSanPhamMapper {
    KhuyenMaiSanPhamResponse toResponse(KhuyenMaiSanPham e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    @Mapping(target = "sanPham", ignore = true)
    @Mapping(target = "danhMuc", ignore = true)
    KhuyenMaiSanPham toEntity(KhuyenMaiSanPhamRequest r);
}
