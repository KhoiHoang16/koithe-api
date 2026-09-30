package com.milktea.catalog.mapper;
import com.milktea.catalog.dto.ProductRequest;
import com.milktea.catalog.dto.ProductResponse;
import com.milktea.catalog.entity.SanPham;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "maDanhMuc", source = "danhMuc.id")
    @Mapping(target = "tenDanhMuc", source = "danhMuc.tenDanhMuc")
    ProductResponse toResponse(SanPham entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "danhMuc", ignore = true)
    void update(ProductRequest request, @MappingTarget SanPham entity);
}
