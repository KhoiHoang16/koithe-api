package com.milktea.catalog.mapper;
import com.milktea.catalog.dto.CategoryRequest;
import com.milktea.catalog.dto.CategoryResponse;
import com.milktea.catalog.entity.LoaiSanPham;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(LoaiSanPham entity);
    @Mapping(target = "id", ignore = true)
    void update(CategoryRequest request, @MappingTarget LoaiSanPham entity);
}
