package com.milktea.shift.service.impl;

import com.milktea.shift.dto.*;
import com.milktea.shift.service.CaLamViecService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CaLamViecServiceImpl implements CaLamViecService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: CRUD ca; create mở ca với thoi_gian_bat_dau/tien_dau_ca; close ghi thoi_gian_ket_thuc,
    // tien_ket_ca và tính chênh lệch tiền mặt dựa trên giao dịch trong ca.
    // Validation rules: thu ngân phải tồn tại/đang hoạt động; không mở đồng thời nhiều ca cho cùng thu ngân;
    // chỉ đóng ca đang mở; số tiền đầu/cuối ca không âm.
    // Các giá trị hợp lệ: trang_thai = DANG_MO, DA_DONG.
    // Liên quan: NguoiDung thu ngân, Order theo ca và Payment transactions để đối soát.

    public List<CaLamViecResponse> findAll() { throw todo(); }
    public CaLamViecResponse findById(Long id) { throw todo(); }
    public CaLamViecResponse create(CaLamViecRequest r) { throw todo(); }
    public CaLamViecResponse update(Long id, CaLamViecRequest r) { throw todo(); }
    public void delete(Long id) { throw todo(); }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
