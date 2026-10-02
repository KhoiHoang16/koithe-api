package com.milktea.inventory.mapper;

import com.milktea.inventory.dto.*;
import com.milktea.inventory.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NhaCungCapMapper {
    NhaCungCapResponse toResponse(NhaCungCap e);

    @Mapping(target = "id", ignore = true)
    NhaCungCap toEntity(NhaCungCapRequest r);
}
