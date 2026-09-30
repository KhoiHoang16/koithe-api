package com.milktea.cart.mapper;

import com.milktea.cart.dto.CartResponse;
import com.milktea.cart.entity.GioHang;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "cartToken", source = "tokenPhien")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "total", ignore = true)
    CartResponse toResponse(GioHang entity);
}
