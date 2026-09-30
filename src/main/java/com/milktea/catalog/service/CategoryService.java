package com.milktea.catalog.service;
import com.milktea.catalog.dto.CategoryRequest;
import com.milktea.catalog.dto.CategoryResponse;
import java.util.List;
public interface CategoryService {
    List<CategoryResponse> list(); CategoryResponse get(Long id); CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request); void delete(Long id);
}
