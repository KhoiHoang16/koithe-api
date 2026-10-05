package com.milktea.table.service;

import com.milktea.table.dto.*;
import java.util.List;

public interface BanService {
    List<BanResponse> findAll();

    BanResponse findById(Long id);

    BanResponse findByQrToken(String token);

    BanResponse create(BanRequest r);

    BanResponse update(Long id, BanRequest r);

    BanResponse updateStatus(Long id, String status);

    void delete(Long id);
}
