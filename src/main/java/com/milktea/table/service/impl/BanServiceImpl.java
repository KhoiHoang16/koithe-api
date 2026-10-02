package com.milktea.table.service.impl;

import com.milktea.table.dto.*;
import com.milktea.table.service.BanService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BanServiceImpl implements BanService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: CRUD bàn; sinh QR token đủ ngẫu nhiên khi tạo/rotate; không để token cũ tiếp tục mở phiên sau rotate;
    // chỉ đổi trạng thái bàn theo quy trình phục vụ và trạng thái đơn đang gắn bàn.
    // Validation rules: so_ban và ma_qr_token duy nhất; không xóa bàn đang có khách/đơn chưa hoàn tất.
    // Các giá trị hợp lệ: trang_thai = TRONG, DANG_CO_KHACH, DA_DAT_TRUOC.
    // Liên quan: Order tham chiếu Ban; luồng QR của Cart/Order sử dụng ma_qr_token.
    public List<BanResponse> findAll() {
        throw todo();
    }

    public BanResponse findById(Long id) {
        throw todo();
    }

    public BanResponse create(BanRequest r) {
        throw todo();
    }

    public BanResponse update(Long id, BanRequest r) {
        throw todo();
    }

    public void delete(Long id) {
        throw todo();
    }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
