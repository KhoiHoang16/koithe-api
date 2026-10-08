package com.milktea.promotion.mapper;

import com.milktea.promotion.dto.KhuyenMaiSanPhamRequest;
import com.milktea.promotion.dto.KhuyenMaiSanPhamResponse;
import com.milktea.promotion.entity.KhuyenMaiSanPham;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface KhuyenMaiSanPhamMapper {
    @Mapping(target = "maChuongTrinh", source = "chuongTrinh.id")
    @Mapping(target = "maSanPham", source = "sanPham.id")
    @Mapping(target = "maDanhMuc", source = "danhMuc.id")
    KhuyenMaiSanPhamResponse toResponse(KhuyenMaiSanPham e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    @Mapping(target = "sanPham", ignore = true)
    @Mapping(target = "danhMuc", ignore = true)
    KhuyenMaiSanPham toEntity(KhuyenMaiSanPhamRequest r);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chuongTrinh", ignore = true)
    @Mapping(target = "sanPham", ignore = true)
    @Mapping(target = "danhMuc", ignore = true)
    void updateEntity(@MappingTarget KhuyenMaiSanPham entity, KhuyenMaiSanPhamRequest r);
}
