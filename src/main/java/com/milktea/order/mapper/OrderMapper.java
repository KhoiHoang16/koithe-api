package com.milktea.order.mapper;

import com.milktea.order.dto.OrderResponse;
import com.milktea.order.entity.DonHang;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "displayCode", source = "maHienThiDon")
    @Mapping(target = "status", source = "trangThai")
    @Mapping(target = "channel", source = "kenhDatHang")
    @Mapping(target = "serviceType", source = "loaiPhucVu")
    @Mapping(target = "customerId", source = "khachHang.id")
    @Mapping(target = "cashierId", source = "thuNgan.id")
    @Mapping(target = "shiftId", source = "caLamViec.id")
    @Mapping(target = "tableId", source = "ban.id")
    @Mapping(target = "merchandiseTotal", source = "tongTienHang")
    @Mapping(target = "discount", source = "soTienGiam")
    @Mapping(target = "payableTotal", source = "tongTienThanhToan")
    @Mapping(target = "createdAt", source = "ngayTao")
    @Mapping(target = "items", ignore = true)
    OrderResponse toResponse(DonHang entity);
}
