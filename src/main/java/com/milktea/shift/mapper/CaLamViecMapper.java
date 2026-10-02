package com.milktea.shift.mapper;

import com.milktea.shift.dto.*;
import com.milktea.shift.entity.CaLamViec;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CaLamViecMapper {
    CaLamViecResponse toResponse(CaLamViec entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "thuNgan", ignore = true)
    CaLamViec toEntity(CaLamViecRequest request);
}
