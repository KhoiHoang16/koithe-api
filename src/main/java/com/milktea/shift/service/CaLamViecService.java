package com.milktea.shift.service;

import com.milktea.shift.dto.*;
import java.util.List;

public interface CaLamViecService {
    List<CaLamViecResponse> findAll();

    List<CaLamViecResponse> findAll(String status);

    CaLamViecResponse findById(Long id);

    CaLamViecResponse findCurrentActive();

    CaLamViecResponse findCurrentActive(Long thuNganId);

    CaLamViecResponse create(CaLamViecRequest r);

    CaLamViecResponse update(Long id, CaLamViecRequest r);

    void delete(Long id);
}
