package com.milktea.catalog.service;
import com.milktea.catalog.dto.*; import com.milktea.common.response.PageResponse; import org.springframework.data.domain.Pageable;
public interface ProductService { PageResponse<ProductResponse> list(Long categoryId,String keyword,Pageable pageable); ProductResponse get(Long id); ProductResponse create(ProductRequest request); ProductResponse update(Long id,ProductRequest request); void delete(Long id); }
