package com.milktea.inventory.service;

import com.milktea.inventory.dto.*;
import com.milktea.common.response.PageResponse;

public interface NhaCungCapService {
    PageResponse<NhaCungCapResponse> getAll(int page, int size);

    NhaCungCapResponse getById(Long id);

    NhaCungCapResponse create(NhaCungCapRequest r);

    NhaCungCapResponse update(Long id, NhaCungCapRequest r);

    void delete(Long id);
}
