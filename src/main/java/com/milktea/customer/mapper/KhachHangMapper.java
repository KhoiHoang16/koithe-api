package com.milktea.customer.mapper;

import com.milktea.customer.dto.*;
import com.milktea.customer.entity.KhachHang;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface KhachHangMapper {
    KhachHangResponse toResponse(KhachHang e);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matKhauMaHoa", ignore = true)
    @Mapping(target = "diemTichLuy", ignore = true)
    @Mapping(target = "hangThanhVien", ignore = true)
    @Mapping(target = "daXacThuc", ignore = true)
    @Mapping(target = "tokenLamMoi", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    KhachHang toEntity(KhachHangCreateRequest r);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "soDienThoai", ignore = true)
    @Mapping(target = "matKhauMaHoa", ignore = true)
    @Mapping(target = "diemTichLuy", ignore = true)
    @Mapping(target = "hangThanhVien", ignore = true)
    @Mapping(target = "daXacThuc", ignore = true)
    @Mapping(target = "tokenLamMoi", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(KhachHangUpdateRequest dto, @MappingTarget KhachHang entity);
}
