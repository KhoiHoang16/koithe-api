package com.milktea.catalog.service;
import com.milktea.catalog.dto.*; import java.util.List;
public interface VariantService { List<VariantResponse> list(Long productId); VariantResponse create(Long productId,VariantRequest request); VariantResponse update(Long id,VariantRequest request); VariantResponse updateStock(Long id,StockRequest request); }
