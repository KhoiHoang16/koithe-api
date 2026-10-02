package com.milktea.promotion.service;

import com.milktea.promotion.dto.*;
import com.milktea.common.response.PageResponse;

public interface ChuongTrinhKhuyenMaiService {
    PageResponse<ChuongTrinhKhuyenMaiResponse> getAll(int page, int size);

    ChuongTrinhKhuyenMaiResponse getById(Long id);

    ChuongTrinhKhuyenMaiResponse create(ChuongTrinhKhuyenMaiRequest r);

    ChuongTrinhKhuyenMaiResponse update(Long id, ChuongTrinhKhuyenMaiRequest r);

    void delete(Long id);
}
