package com.milktea.cart.mapper;

import com.milktea.cart.dto.CartItemResponse;
import com.milktea.cart.dto.CartToppingResponse;
import com.milktea.cart.entity.ChiTietGioHang;
import com.milktea.cart.entity.ToppingGioHang;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "variantId", source = "bienThe.id")
    @Mapping(target = "productName", source = "bienThe.sanPham.tenSanPham")
    @Mapping(target = "size", source = "bienThe.kichCo")
    @Mapping(target = "quantity", source = "soLuong")
    @Mapping(target = "sugarLevel", source = "mucDuong")
    @Mapping(target = "iceLevel", source = "mucDa")
    @Mapping(target = "note", source = "ghiChuMon")
    @Mapping(target = "toppings", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "lineTotal", ignore = true)
    CartItemResponse toResponse(ChiTietGioHang entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "toppingId", source = "topping.id")
    @Mapping(target = "name", source = "topping.tenTopping")
    @Mapping(target = "quantity", source = "soLuong")
    @Mapping(target = "unitPrice", source = "topping.giaBan")
    CartToppingResponse toResponse(ToppingGioHang entity);
}
