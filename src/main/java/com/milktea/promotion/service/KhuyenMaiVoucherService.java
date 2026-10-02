package com.milktea.promotion.service;

import com.milktea.promotion.dto.*;
import com.milktea.common.response.PageResponse;

public interface KhuyenMaiVoucherService {
    PageResponse<KhuyenMaiVoucherResponse> getAll(int page, int size);

    KhuyenMaiVoucherResponse getById(Long id);

    KhuyenMaiVoucherResponse create(KhuyenMaiVoucherRequest r);

    KhuyenMaiVoucherResponse update(Long id, KhuyenMaiVoucherRequest r);

    void delete(Long id);
}
