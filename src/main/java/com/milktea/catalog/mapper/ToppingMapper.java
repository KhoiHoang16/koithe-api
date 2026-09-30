package com.milktea.catalog.mapper;
import com.milktea.catalog.dto.ToppingRequest;
import com.milktea.catalog.dto.ToppingResponse;
import com.milktea.catalog.entity.Topping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface ToppingMapper {
    ToppingResponse toResponse(Topping entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conHang", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(ToppingRequest request, @MappingTarget Topping entity);
}
