package com.milktea.order.mapper;

import com.milktea.order.dto.OrderToppingResponse;
import com.milktea.order.entity.ToppingDonHang;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderToppingMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "toppingId", source = "topping.id")
    @Mapping(target = "name", source = "topping.tenTopping")
    @Mapping(target = "quantity", source = "soLuong")
    @Mapping(target = "unitPrice", source = "donGia")
    @Mapping(target = "lineTotal", expression = "java(entity.getDonGia().multiply(java.math.BigDecimal.valueOf(entity.getSoLuong())))")
    OrderToppingResponse toResponse(ToppingDonHang entity);
}
