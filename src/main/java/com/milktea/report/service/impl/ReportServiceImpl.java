package com.milktea.report.service.impl;

import com.milktea.report.service.ReportService;
import org.springframework.stereotype.Service;

/**
 * ARCHITECTURE NOTE:
 * Reports read data owned by order, payment, and catalog; this service currently has no direct
 * dependencies on their services and should remain query/read-model oriented.
 * This is acceptable in the current Modular Monolith stage.
 *
 * As the business grows, consider:
 * - Defining report-specific projections/read repositories or public read facades.
 * - Using domain events to maintain a reporting read model when query load requires it.
 * - Splitting a module into a microservice only when independent deployment is justified.
 *
 * No refactor is required now; do not call other modules' business services for report queries.
 */
@Service
public class ReportServiceImpl implements ReportService {
    // TODO: [Partner] Implement business logic.
    // Yêu cầu nghiệp vụ: tạo truy vấn chỉ đọc cho doanh thu theo ngày/tuần/tháng, số đơn theo trạng thái và sản phẩm bán chạy;
    // hỗ trợ khoảng thời gian và phân trang/kích thước top; trả projection/DTO thay vì tải toàn bộ entity.
    // Validation rules: from <= to; chuẩn hóa timezone theo cấu hình cửa hàng; loại trừ đơn đã hủy theo định nghĩa báo cáo.
    // Các giá trị liên quan: don_hang.trang_thai, kenh_dat_hang, chi_tiet_don_hang.so_luong/thanh_tien.
    // Liên quan: chỉ đọc dữ liệu Order/Catalog qua query/report repository; không gọi trực tiếp service nghiệp vụ module khác.
    public Object summary() {
        throw new UnsupportedOperationException("TODO: Implement business logic");
    }
}
