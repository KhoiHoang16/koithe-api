package com.milktea.inventory.service;

import com.milktea.inventory.dto.*;
import com.milktea.common.response.PageResponse;

public interface ChiTietPhieuNhapService {
    PageResponse<ChiTietPhieuNhapResponse> getAllByMaPhieuNhap(Long maPhieuNhap, int page, int size);

    ChiTietPhieuNhapResponse getById(Long id);

    ChiTietPhieuNhapResponse create(ChiTietPhieuNhapRequest r);

    ChiTietPhieuNhapResponse update(Long id, ChiTietPhieuNhapRequest r);

    void delete(Long id);
}
