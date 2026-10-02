package com.milktea.payment.mapper;

import com.milktea.payment.dto.*;
import com.milktea.payment.entity.GiaoDichThanhToan;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GiaoDichThanhToanMapper {
    GiaoDichThanhToanResponse toResponse(GiaoDichThanhToan entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "donHang", ignore = true)
    GiaoDichThanhToan toEntity(GiaoDichThanhToanRequest request);
}
