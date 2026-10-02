package com.milktea.inventory.service;

import com.milktea.inventory.dto.*;
import com.milktea.common.response.PageResponse;
import java.time.Instant;

public interface PhieuNhapHangService {
    PageResponse<PhieuNhapHangResponse> getAll(Long supplierId, Instant fromDate, Instant toDate, String trangThai,
            int page, int size);

    PhieuNhapHangResponse getById(Long id);

    PhieuNhapHangResponse create(PhieuNhapHangRequest r);

    PhieuNhapHangResponse update(Long id, PhieuNhapHangRequest r);

    void delete(Long id);

    PhieuNhapHangResponse approve(Long id);
}
