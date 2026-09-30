package com.milktea.catalog.service;
import com.milktea.catalog.dto.*; import java.util.List;
public interface ToppingService { List<ToppingResponse> list(); ToppingResponse create(ToppingRequest request); ToppingResponse update(Long id,ToppingRequest request); void delete(Long id); }
