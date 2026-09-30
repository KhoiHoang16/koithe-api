package com.milktea.catalog.mapper;
import com.milktea.catalog.dto.VariantRequest;
import com.milktea.catalog.dto.VariantResponse;
import com.milktea.catalog.entity.BienTheSanPham;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface VariantMapper {
    @Mapping(target = "maSanPham", source = "sanPham.id")
    VariantResponse toResponse(BienTheSanPham entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sanPham", ignore = true)
    @Mapping(target = "conHang", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(VariantRequest request, @MappingTarget BienTheSanPham entity);
}
