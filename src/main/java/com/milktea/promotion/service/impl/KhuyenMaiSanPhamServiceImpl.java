package com.milktea.promotion.service.impl;

import com.milktea.promotion.dto.*;
import com.milktea.promotion.service.KhuyenMaiSanPhamService;
import com.milktea.common.response.PageResponse;
import org.springframework.stereotype.Service;

/**
 * ARCHITECTURE NOTE:
 * The promotion module depends on catalog through product/category-targeted promotion data.
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
public class KhuyenMaiSanPhamServiceImpl implements KhuyenMaiSanPhamService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: CRUD phân trang khuyến mãi sản phẩm/danh mục; nạp chương trình cha và kiểm tra hiệu lực trước
    // khi áp dụng; quy định thứ tự kết hợp với các khuyến mãi hóa đơn.
    // Validation rules: chương trình phải tồn tại; phải có ma_san_pham hoặc ma_danh_muc (schema cho phép cả hai);
    // gia_tri_giam không âm và phần trăm không vượt 100.
    // Các giá trị hợp lệ: loai_giam_gia = PHAN_TRAM, TIEN_CO_DINH.
    // Liên quan: catalog SanPham/LoaiSanPham và ChuongTrinhKhuyenMai; Order tiêu thụ kết quả giảm giá.
    public PageResponse<KhuyenMaiSanPhamResponse> getAll(int page, int size) {
        throw todo();
    }

    public KhuyenMaiSanPhamResponse getById(Long id) {
        throw todo();
    }

    public KhuyenMaiSanPhamResponse create(KhuyenMaiSanPhamRequest r) {
        throw todo();
    }

    public KhuyenMaiSanPhamResponse update(Long id, KhuyenMaiSanPhamRequest r) {
        throw todo();
    }

    public void delete(Long id) {
        throw todo();
    }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
