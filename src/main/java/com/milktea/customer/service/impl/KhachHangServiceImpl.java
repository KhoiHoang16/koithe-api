package com.milktea.customer.service.impl;

import com.milktea.customer.dto.*;
import com.milktea.customer.service.KhachHangService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KhachHangServiceImpl implements KhachHangService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: CRUD khách hàng; cộng điểm từ đơn đã thanh toán/hoàn thành đúng một lần; nâng hạng theo ngưỡng
    // được Product Owner xác nhận; hỗ trợ lịch sử đổi/trừ điểm nếu có chính sách hoàn đơn.
    // Validation rules: so_dien_thoai duy nhất; validate email; không nhận mat_khau_ma_hoa hoặc token_lam_moi từ response;
    // lưu mật khẩu bằng PasswordEncoder và không ghi token nhạy cảm vào log.
    // Các giá trị hợp lệ: hang_thanh_vien = DONG, BAC, VANG, KIM_CUONG; da_xac_thuc = true/false.
    // Liên quan: Order (điểm phát sinh từ đơn), Auth/User (định danh và xác thực khách hàng).
    // Idempotency: cùng một đơn không được cộng điểm nhiều lần khi retry.

    public List<KhachHangResponse> findAll() { throw todo(); }
    public KhachHangResponse findById(Long id) { throw todo(); }
    public KhachHangResponse create(KhachHangRequest r) { throw todo(); }
    public KhachHangResponse update(Long id, KhachHangRequest r) { throw todo(); }
    public void delete(Long id) { throw todo(); }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
