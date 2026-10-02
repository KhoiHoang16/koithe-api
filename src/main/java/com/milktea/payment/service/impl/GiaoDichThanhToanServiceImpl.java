package com.milktea.payment.service.impl;

import com.milktea.payment.dto.*;
import com.milktea.payment.gateway.PaymentGatewayFactory;
import com.milktea.payment.service.GiaoDichThanhToanService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * ARCHITECTURE NOTE:
 * The payment module depends on order through the GiaoDichThanhToan.donHang entity association
 * and will need order payment state when completing the payment workflow.
 * This is acceptable in the current Modular Monolith stage.
 *
 * As the business grows, consider:
 * - Exposing a public order facade for payment-state checks and updates.
 * - Using domain events for asynchronous payment completion notifications.
 * - Splitting a module into a microservice only when independent deployment is justified.
 *
 * No refactor is required now; keep the dependency explicit and controlled.
 */
@Service
public class GiaoDichThanhToanServiceImpl implements GiaoDichThanhToanService {
    private final PaymentGatewayFactory gatewayFactory;

    public GiaoDichThanhToanServiceImpl(PaymentGatewayFactory gatewayFactory) {
        this.gatewayFactory = gatewayFactory;
    }

    // TODO: [Partner] Implement business logic.
    //
    // Yêu cầu nghiệp vụ:
    // 1. Khi tạo giao dịch, xác nhận đơn tồn tại và chưa thanh toán đủ; lưu trạng thái CHO_XU_LY.
    // 2. Với CHUYEN_KHOAN_QR, tạo nội dung VietQR theo chuẩn EMVCo; với cổng ngoài, gọi gateway tương ứng.
    // 3. Callback phải xác thực chữ ký, cập nhật giao dịch/đơn hàng trong transaction và xử lý idempotency.
    // 4. Chỉ đánh dấu đơn DA_THANH_TOAN khi tổng thanh toán thành công đã đủ; không xóa lịch sử giao dịch thành công.
    //
    // Validation rules:
    // - so_tien > 0 và không vượt số dư cần thanh toán của đơn hàng.
    // - ma_don_hang phải tồn tại; callback tham chiếu giao dịch đã khởi tạo.
    //
    // Các giá trị hợp lệ:
    // - phuong_thuc_thanh_toan: TIEN_MAT, CHUYEN_KHOAN_QR, MOMO, VNPAY, THE_NGAN_HANG.
    // - trang_thai: CHO_XU_LY, THANH_CONG, THAT_BAI, HOAN_TIEN.
    //
    // Liên quan: Order module (trạng thái đơn); PaymentGatewayFactory/PaymentGateway (Strategy/Adapter).
    // Idempotency: dùng mã tham chiếu provider duy nhất để chống callback lặp và double payment.
    public List<GiaoDichThanhToanResponse> findAll() {
        throw todo();
    }

    public GiaoDichThanhToanResponse findById(Long id) {
        throw todo();
    }

    public GiaoDichThanhToanResponse create(GiaoDichThanhToanRequest request) {
        // TODO: [Partner] Sau khi xác thực order và số tiền còn phải thu, chọn strategy theo method rồi xử lý kết quả.
        // Ví dụ với record DTO hiện tại:
        // PaymentGateway gateway = gatewayFactory.getGateway(request.phuongThucThanhToan());
        // GatewayPaymentRequest gatewayRequest = GatewayPaymentRequest.builder()
        //         .orderId(request.maDonHang()).amount(request.soTien())
        //         .description(request.noiDungVietqr()).build();
        // GatewayPaymentResult result = gateway.processPayment(gatewayRequest);
        // Persist giao dịch/trạng thái dựa trên result; không coi redirect/QR là thanh toán thành công.
        throw todo();
    }

    public GiaoDichThanhToanResponse update(Long id, GiaoDichThanhToanRequest request) {
        throw todo();
    }

    public void delete(Long id) {
        throw todo();
    }

    private UnsupportedOperationException todo() {
        return new UnsupportedOperationException("TODO: Implement business logic");
    }
}
