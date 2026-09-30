package com.milktea.order.mapper;

import com.milktea.order.dto.OrderItemResponse;
import com.milktea.order.entity.ChiTietDonHang;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "variantId", source = "bienThe.id")
    @Mapping(target = "productName", source = "bienThe.sanPham.tenSanPham")
    @Mapping(target = "size", source = "bienThe.kichCo")
    @Mapping(target = "quantity", source = "soLuong")
    @Mapping(target = "unitPrice", source = "donGia")
    @Mapping(target = "lineTotal", source = "thanhTien")
    @Mapping(target = "sugarLevel", source = "mucDuong")
    @Mapping(target = "iceLevel", source = "mucDa")
    @Mapping(target = "note", source = "ghiChuMon")
    @Mapping(target = "toppings", ignore = true)
    OrderItemResponse toResponse(ChiTietDonHang entity);
}
