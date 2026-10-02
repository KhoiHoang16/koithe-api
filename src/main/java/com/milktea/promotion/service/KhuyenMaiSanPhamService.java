package com.milktea.promotion.service;

import com.milktea.promotion.dto.*;
import com.milktea.common.response.PageResponse;

public interface KhuyenMaiSanPhamService {
    PageResponse<KhuyenMaiSanPhamResponse> getAll(int page, int size);

    KhuyenMaiSanPhamResponse getById(Long id);

    KhuyenMaiSanPhamResponse create(KhuyenMaiSanPhamRequest r);

    KhuyenMaiSanPhamResponse update(Long id, KhuyenMaiSanPhamRequest r);

    void delete(Long id);
}
