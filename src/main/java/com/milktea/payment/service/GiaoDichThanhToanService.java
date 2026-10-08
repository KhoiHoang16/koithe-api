package com.milktea.payment.service;

import com.milktea.payment.dto.*;
import java.util.List;
import java.util.Map;

public interface GiaoDichThanhToanService {
    List<GiaoDichThanhToanResponse> findAll();

    GiaoDichThanhToanResponse findById(Long id);

    GiaoDichThanhToanResponse create(GiaoDichThanhToanRequest request);

    GiaoDichThanhToanResponse update(Long id, GiaoDichThanhToanRequest request);

    void delete(Long id);

    GiaoDichThanhToanResponse handleCallback(String method, Map<String, String> callbackParams);

    GiaoDichThanhToanResponse simulateSuccess(Long orderId, String method);
}

