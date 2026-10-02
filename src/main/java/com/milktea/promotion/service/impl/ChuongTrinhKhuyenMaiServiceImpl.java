package com.milktea.promotion.service.impl;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.service.ChuongTrinhKhuyenMaiService;
import com.milktea.common.response.PageResponse;
import org.springframework.stereotype.Service;

/**
 * ARCHITECTURE NOTE:
 * The promotion module has a catalog dependency through product/category-targeted promotions
 * (KhuyenMaiSanPham references catalog entities); this service coordinates the promotion program.
 * This is acceptable in the current Modular Monolith stage.
 *
 * As the business grows, consider:
 * - Exposing a public catalog facade for product/category validation.
 * - Using domain events for asynchronous cross-module communication.
 * - Splitting a module into a microservice only when independent deployment is justified.
 *
 * No refactor is required now; keep these dependencies explicit and controlled.
 */
@Service
public class ChuongTrinhKhuyenMaiServiceImpl implements ChuongTrinhKhuyenMaiService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: phân trang danh sách; tìm theo id; tạo/cập nhật chương trình; xóa mềm và kiểm tra chương trình
    // đang được các loại khuyến mãi con hoặc đơn hàng tham chiếu trước khi xóa.
    // Validation rules: tên không rỗng; ngay_bat_dau <= ngay_ket_thuc; page >= 0, size > 0; không lộ deleted_at.
    // Các giá trị hợp lệ: dang_hoat_dong = true/false.
    // Liên quan: KhuyenMaiSanPham, KhuyenMaiHoaDon, KhuyenMaiVoucher; Order tham chiếu voucher thuộc chương trình.
    public PageResponse<ChuongTrinhKhuyenMaiResponse> getAll(int page, int size) {
        throw todo();
    }

    public ChuongTrinhKhuyenMaiResponse getById(Long id) {
        throw todo();
    }

    public ChuongTrinhKhuyenMaiResponse create(ChuongTrinhKhuyenMaiRequest r) {
        throw todo();
    }

    public ChuongTrinhKhuyenMaiResponse update(Long id, ChuongTrinhKhuyenMaiRequest r) {
        throw todo();
    }

    public void delete(Long id) {
        throw todo();
    }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
