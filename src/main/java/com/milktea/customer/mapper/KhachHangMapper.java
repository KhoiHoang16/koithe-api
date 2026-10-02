package com.milktea.customer.mapper;

import com.milktea.customer.dto.*;
import com.milktea.customer.entity.KhachHang;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface KhachHangMapper {
    KhachHangResponse toResponse(KhachHang e);

    @Mapping(target = "id", ignore = true)
    KhachHang toEntity(KhachHangRequest r);
}
