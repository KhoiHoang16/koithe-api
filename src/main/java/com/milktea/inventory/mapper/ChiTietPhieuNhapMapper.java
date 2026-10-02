package com.milktea.inventory.mapper;

import com.milktea.inventory.dto.*;
import com.milktea.inventory.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ChiTietPhieuNhapMapper {
    ChiTietPhieuNhapResponse toResponse(ChiTietPhieuNhap e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phieuNhapHang", ignore = true)
    @Mapping(target = "bienThe", ignore = true)
    @Mapping(target = "topping", ignore = true)
    ChiTietPhieuNhap toEntity(ChiTietPhieuNhapRequest r);
}
