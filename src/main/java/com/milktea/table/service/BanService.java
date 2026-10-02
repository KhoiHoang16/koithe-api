package com.milktea.table.service;

import com.milktea.table.dto.*;
import java.util.List;

public interface BanService {
    List<BanResponse> findAll();

    BanResponse findById(Long id);

    BanResponse create(BanRequest r);

    BanResponse update(Long id, BanRequest r);

    void delete(Long id);
}
