package com.milktea.promotion.service.impl;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.service.KhuyenMaiHoaDonService;
import com.milktea.common.response.PageResponse;
import org.springframework.stereotype.Service;

/**
 * ARCHITECTURE NOTE:
 * The promotion module has a catalog dependency through its product-promotion flow; invoice
 * discounts remain in this module and are combined at the promotion boundary.
 * This is acceptable in the current Modular Monolith stage.
 *
 * As the business grows, consider:
 * - Exposing public catalog/promotion facades for cross-module reads.
 * - Using domain events for asynchronous cross-module communication.
 * - Splitting a module into a microservice only when independent deployment is justified.
 *
 * No refactor is required now; keep these dependencies explicit and controlled.
 */
@Service
public class KhuyenMaiHoaDonServiceImpl implements KhuyenMaiHoaDonService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: CRUD phân trang mức giảm hóa đơn; kiểm tra ngưỡng hóa đơn tối thiểu và áp dụng mức trần giảm
    // khi tính discount; phối hợp thứ tự với khuyến mãi sản phẩm và voucher.
    // Validation rules: don_hang_toi_thieu >= 0; gia_tri_giam > 0; muc_giam_toi_da nếu có phải >= 0; chương trình tồn tại.
    // Các giá trị hợp lệ: loai_giam_gia = PHAN_TRAM, TIEN_CO_DINH.
    // Liên quan: ChuongTrinhKhuyenMai và Order; chỉ tính trên số tiền đủ điều kiện theo quy tắc bán hàng.
    public PageResponse<KhuyenMaiHoaDonResponse> getAll(int page, int size) {
        throw todo();
    }

    public KhuyenMaiHoaDonResponse getById(Long id) {
        throw todo();
    }

    public KhuyenMaiHoaDonResponse create(KhuyenMaiHoaDonRequest r) {
        throw todo();
    }

    public KhuyenMaiHoaDonResponse update(Long id, KhuyenMaiHoaDonRequest r) {
        throw todo();
    }

    public void delete(Long id) {
        throw todo();
    }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
