package com.milktea.promotion.service.impl;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.service.KhuyenMaiVoucherService;
import com.milktea.common.response.PageResponse;
import org.springframework.stereotype.Service;

/**
 * ARCHITECTURE NOTE:
 * The promotion module has a catalog dependency through product-promotion eligibility; voucher
 * validation is exposed to order/payment through the promotion boundary.
 * This is acceptable in the current Modular Monolith stage.
 *
 * As the business grows, consider:
 * - Exposing public catalog and promotion facades for cross-module operations.
 * - Using domain events for asynchronous cross-module communication.
 * - Splitting a module into a microservice only when independent deployment is justified.
 *
 * No refactor is required now; keep these dependencies explicit and controlled.
 */
@Service
public class KhuyenMaiVoucherServiceImpl implements KhuyenMaiVoucherService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ:
    // 1. Tạo/cập nhật voucher duy nhất theo ma_code và liên kết với chương trình khuyến mãi.
    // 2. Khi validate: kiểm tra chương trình đang bật và now nằm trong khoảng ngày hiệu lực.
    // 3. Kiểm tra lượt dùng, don_hang_toi_thieu, rồi tính giảm theo phần trăm hoặc số tiền cố định và mức giảm tối đa.
    // 4. Không để số tiền giảm âm hoặc lớn hơn giá trị đơn; tăng lượt dùng nguyên tử khi áp dụng.
    // 5. Hoàn lượt dùng khi đơn bị hủy theo chính sách đã chốt; cân nhắc API validate riêng ngoài CRUD.
    //
    // Validation rules: ma_code không rỗng/duy nhất; giá trị giảm > 0; số tiền tối thiểu >= 0;
    // gioi_han_su_dung nếu có phải dương và so_luot_da_dung không vượt giới hạn.
    // Các giá trị hợp lệ: loai_giam_gia = PHAN_TRAM, TIEN_CO_DINH.
    // Liên quan: ChuongTrinhKhuyenMai, Order, Payment; kiểm soát đồng thời khi nhiều đơn dùng voucher.
    // Idempotency: không tăng so_luot_da_dung lặp khi retry cùng một đơn.
    public PageResponse<KhuyenMaiVoucherResponse> getAll(int page, int size) {
        throw todo();
    }

    public KhuyenMaiVoucherResponse getById(Long id) {
        throw todo();
    }

    public KhuyenMaiVoucherResponse create(KhuyenMaiVoucherRequest r) {
        throw todo();
    }

    public KhuyenMaiVoucherResponse update(Long id, KhuyenMaiVoucherRequest r) {
        throw todo();
    }

    public void delete(Long id) {
        throw todo();
    }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
