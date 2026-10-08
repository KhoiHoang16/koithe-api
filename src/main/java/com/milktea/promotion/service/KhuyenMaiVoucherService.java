package com.milktea.promotion.service;

import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.KhuyenMaiVoucherRequest;
import com.milktea.promotion.dto.KhuyenMaiVoucherResponse;
import com.milktea.promotion.dto.ValidateVoucherRequest;
import com.milktea.promotion.dto.ValidateVoucherResponse;

public interface KhuyenMaiVoucherService {
    PageResponse<KhuyenMaiVoucherResponse> getAll(int page, int size);

    KhuyenMaiVoucherResponse getById(Long id);

    KhuyenMaiVoucherResponse create(KhuyenMaiVoucherRequest r);

    KhuyenMaiVoucherResponse update(Long id, KhuyenMaiVoucherRequest r);

    void delete(Long id);

    ValidateVoucherResponse validateVoucher(ValidateVoucherRequest r);

    ValidateVoucherResponse applyVoucher(ValidateVoucherRequest r);

    void releaseVoucher(String maCode);
}
