package com.milktea.table.mapper;

import com.milktea.table.dto.*;
import com.milktea.table.entity.Ban;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BanMapper {
    BanResponse toResponse(Ban e);

    @Mapping(target = "id", ignore = true)
    Ban toEntity(BanRequest r);
}
