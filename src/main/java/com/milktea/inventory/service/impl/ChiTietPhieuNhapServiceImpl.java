package com.milktea.inventory.service.impl;

import com.milktea.inventory.dto.*;
import com.milktea.inventory.service.ChiTietPhieuNhapService;
import com.milktea.common.response.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class ChiTietPhieuNhapServiceImpl implements ChiTietPhieuNhapService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: liệt kê/phân trang dòng theo maPhieuNhap; CRUD dòng chỉ khi phiếu cha đang CHO_DUYET;
    // cập nhật tong_tien của phiếu khi thêm/sửa/xóa dòng.
    // Validation rules: phiếu nhập phải tồn tại và chưa duyệt; số lượng/đơn giá dương; ít nhất một trong ma_bien_the hoặc
    // ma_topping phải có (schema cho phép cả hai, cần chốt chính sách nếu muốn bắt buộc đúng một loại).
    // Liên quan: PhieuNhapHang, catalog BienTheSanPham/Topping; tồn kho chỉ được cộng khi phiếu được approve.
    public PageResponse<ChiTietPhieuNhapResponse> getAllByMaPhieuNhap(Long maPhieuNhap, int page, int size) {
        return null;
    }

    public ChiTietPhieuNhapResponse getById(Long id) {
        return null;
    }

    public ChiTietPhieuNhapResponse create(ChiTietPhieuNhapRequest r) {
        return null;
    }

    public ChiTietPhieuNhapResponse update(Long id, ChiTietPhieuNhapRequest r) {
        return null;
    }

    public void delete(Long id) {
        throw new UnsupportedOperationException("TODO: Implement business logic");
    }
}
