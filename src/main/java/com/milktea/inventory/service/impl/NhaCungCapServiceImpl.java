package com.milktea.inventory.service.impl;

import com.milktea.inventory.dto.*;
import com.milktea.inventory.service.NhaCungCapService;
import com.milktea.common.response.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class NhaCungCapServiceImpl implements NhaCungCapService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: CRUD phân trang nhà cung cấp; hỗ trợ tìm kiếm theo tên/điện thoại nếu cần; xóa mềm hoặc từ chối
    // xóa khi còn phiếu nhập tham chiếu.
    // Validation rules: tên và số điện thoại bắt buộc; kiểm tra định dạng email; chuẩn hóa khoảng trắng; mã số thuế nếu có.
    // Các giá trị hợp lệ: dang_hop_tac = true/false.
    // Liên quan: PhieuNhapHang giữ FK ma_nha_cung_cap; không xóa cứng nhà cung cấp đã có lịch sử nhập hàng.
    public PageResponse<NhaCungCapResponse> getAll(int page, int size) {
        return null;
    }

    public NhaCungCapResponse getById(Long id) {
        return null;
    }

    public NhaCungCapResponse create(NhaCungCapRequest r) {
        return null;
    }

    public NhaCungCapResponse update(Long id, NhaCungCapRequest r) {
        return null;
    }

    public void delete(Long id) {
        throw new UnsupportedOperationException("TODO: Implement business logic");
    }
}
