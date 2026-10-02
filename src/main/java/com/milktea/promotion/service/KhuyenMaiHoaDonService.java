package com.milktea.promotion.service;

import com.milktea.promotion.dto.*;
import com.milktea.common.response.PageResponse;

public interface KhuyenMaiHoaDonService {
    PageResponse<KhuyenMaiHoaDonResponse> getAll(int page, int size);

    KhuyenMaiHoaDonResponse getById(Long id);

    KhuyenMaiHoaDonResponse create(KhuyenMaiHoaDonRequest r);

    KhuyenMaiHoaDonResponse update(Long id, KhuyenMaiHoaDonRequest r);

    void delete(Long id);
}
