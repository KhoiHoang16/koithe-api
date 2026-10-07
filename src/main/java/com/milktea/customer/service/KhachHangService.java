package com.milktea.customer.service;

import com.milktea.customer.dto.*;
import java.util.List;

public interface KhachHangService {
    List<KhachHangResponse> findAll(String phone);
    KhachHangResponse findById(Long id);
    KhachHangResponse findByPhone(String phone);
    KhachHangResponse create(KhachHangCreateRequest request);
    KhachHangResponse update(Long id, KhachHangUpdateRequest request);
    void delete(Long id);
}
