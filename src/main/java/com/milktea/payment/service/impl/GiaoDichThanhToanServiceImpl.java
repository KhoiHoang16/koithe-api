package com.milktea.payment.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.order.entity.DonHang;
import com.milktea.order.repository.DonHangRepository;
import com.milktea.order.service.OrderService;
import com.milktea.payment.dto.*;
import com.milktea.payment.entity.GiaoDichThanhToan;
import com.milktea.payment.gateway.PaymentGateway;
import com.milktea.payment.gateway.PaymentGatewayFactory;
import com.milktea.payment.gateway.dto.GatewayPaymentRequest;
import com.milktea.payment.gateway.dto.GatewayPaymentResult;
import com.milktea.payment.mapper.GiaoDichThanhToanMapper;
import com.milktea.payment.repository.GiaoDichThanhToanRepository;
import com.milktea.payment.gateway.MomoProperties;
import com.milktea.payment.gateway.VietQrProperties;
import com.milktea.payment.gateway.VnPayProperties;
import com.milktea.payment.gateway.impl.MomoPaymentGateway;
import com.milktea.payment.gateway.impl.VnPayPaymentGateway;
import com.milktea.payment.service.GiaoDichThanhToanService;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class GiaoDichThanhToanServiceImpl implements GiaoDichThanhToanService {
    private final PaymentGatewayFactory gatewayFactory;
    private final GiaoDichThanhToanRepository paymentRepository;
    private final DonHangRepository donHangRepository;
    private final OrderService orderService;
    private final GiaoDichThanhToanMapper paymentMapper;
    private final VnPayProperties vnPayProperties;
    private final MomoProperties momoProperties;
    private final VietQrProperties vietQrProperties;

    @Override
    @Transactional(readOnly = true)
    public List<GiaoDichThanhToanResponse> findAll() {
        return paymentRepository.findAllByDeletedAtIsNull().stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GiaoDichThanhToanResponse findById(Long id) {
        GiaoDichThanhToan payment = paymentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch với ID: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public GiaoDichThanhToanResponse create(GiaoDichThanhToanRequest request) {
        // 1. Kiểm tra đơn hàng tồn tại
        DonHang order = donHangRepository.findByIdAndDeletedAtIsNull(request.maDonHang())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + request.maDonHang()));

        // 2. Đơn hàng phải đang ở trạng thái 'CHO_XAC_NHAN'
        if (!"CHO_XAC_NHAN".equals(order.getTrangThai())) {
            if ("DA_THANH_TOAN".equals(order.getTrangThai())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng này đã được thanh toán trước đó");
            }
            if ("DA_HUY".equals(order.getTrangThai())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng đã bị hủy, không thể thanh toán");
            }
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng không ở trạng thái chờ thanh toán: " + order.getTrangThai());
        }

        // 3. Kiểm tra số tiền thanh toán (100% tiền đơn hàng, bao gồm cả case đơn 0đ do voucher)
        if (order.getTongTienThanhToan().compareTo(BigDecimal.ZERO) == 0) {
            if (request.soTien().compareTo(BigDecimal.ZERO) != 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng miễn phí (0đ), số tiền thanh toán phải bằng 0");
            }
        } else {
            if (request.soTien().compareTo(order.getTongTienThanhToan()) != 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        String.format("Số tiền thanh toán (%s) phải bằng đúng tổng tiền đơn hàng (%s)",
                                request.soTien(), order.getTongTienThanhToan()));
            }
        }

        // 4. Lấy Strategy tương ứng và thực thi thanh toán
        PaymentGateway gateway = gatewayFactory.getGateway(request.phuongThucThanhToan());
        GatewayPaymentRequest gatewayRequest = GatewayPaymentRequest.builder()
                .orderId(order.getId())
                .orderReference(order.getMaHienThiDon())
                .amount(request.soTien())
                .description(request.noiDungVietqr())
                .build();

        GatewayPaymentResult result = gateway.processPayment(gatewayRequest);

        // 5. Khởi tạo và lưu giao dịch
        GiaoDichThanhToan payment = new GiaoDichThanhToan();
        payment.setDonHang(order);
        payment.setPhuongThucThanhToan(gateway.getMethod());
        payment.setSoTien(request.soTien());
        payment.setNoiDungVietqr(result.transactionReference());

        boolean isCash = "TIEN_MAT".equals(gateway.getMethod());
        if (isCash) {
            if (result.success()) {
                payment.setTrangThai("THANH_CONG");
                payment.setThoiGianThanhToan(Instant.now());
            } else {
                payment.setTrangThai("THAT_BAI");
            }
        } else {
            // Với các cổng online như VNPAY, MOMO: Giao dịch ở trạng thái CHO_XU_LY chờ khách quét mã/thanh toán
            payment.setTrangThai("CHO_XU_LY");
        }

        payment = paymentRepository.save(payment);

        // 6. Cập nhật đơn hàng (với tiền mặt thành công ngay lập tức; với online thì chờ callback/IPN)
        if (isCash && result.success()) {
            orderService.updateStatus(order.getId(), "DA_THANH_TOAN");
        }

        GiaoDichThanhToanResponse response = paymentMapper.toResponse(payment);
        if (result.redirectUrl() != null) {
            response = new GiaoDichThanhToanResponse(
                    payment.getId(),
                    order.getId(),
                    payment.getPhuongThucThanhToan(),
                    payment.getSoTien(),
                    payment.getNoiDungVietqr(),
                    payment.getTrangThai(),
                    payment.getThoiGianThanhToan(),
                    result.redirectUrl());
        }

        return response;
    }

    @Override
    @Transactional
    public GiaoDichThanhToanResponse handleCallback(String method, java.util.Map<String, String> callbackParams) {
        PaymentGateway gateway = gatewayFactory.getGateway(method);
        GatewayPaymentResult result = gateway.handleCallback(callbackParams);

        String txnRef = result.transactionReference();
        if (txnRef == null || txnRef.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Thiếu mã tham chiếu giao dịch trong kết quả thanh toán");
        }

        // Trích xuất orderId từ transactionReference (dạng "orderId_timestamp" hoặc "orderId")
        Long orderId;
        try {
            String idPart = txnRef.contains("_") ? txnRef.substring(0, txnRef.indexOf('_')) : txnRef;
            orderId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã tham chiếu giao dịch không hợp lệ: " + txnRef);
        }

        DonHang order = donHangRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng cho callback: " + orderId));

        // Lấy giao dịch gần nhất của đơn hàng
        List<GiaoDichThanhToan> payments = paymentRepository.findAllByDonHangIdAndDeletedAtIsNull(order.getId());
        GiaoDichThanhToan payment = payments.stream()
                .filter(p -> method.equals(p.getPhuongThucThanhToan()))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .findFirst()
                .orElseGet(() -> {
                    GiaoDichThanhToan newP = new GiaoDichThanhToan();
                    newP.setDonHang(order);
                    newP.setPhuongThucThanhToan(method);
                    newP.setSoTien(order.getTongTienThanhToan());
                    return newP;
                });

        // Xử lý Idempotency: nếu đơn hàng đã được cập nhật DA_THANH_TOAN rồi, không cộng/trừ lần 2
        if ("DA_THANH_TOAN".equals(order.getTrangThai())) {
            return paymentMapper.toResponse(payment);
        }

        if (result.success()) {
            payment.setTrangThai("THANH_CONG");
            payment.setThoiGianThanhToan(Instant.now());
            payment = paymentRepository.save(payment);

            if ("CHO_XAC_NHAN".equals(order.getTrangThai())) {
                orderService.updateStatus(order.getId(), "DA_THANH_TOAN");
            }
        } else {
            payment.setTrangThai("THAT_BAI");
            payment = paymentRepository.save(payment);
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public GiaoDichThanhToanResponse update(Long id, GiaoDichThanhToanRequest request) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, "Không hỗ trợ cập nhật giao dịch thanh toán trực tiếp");
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GiaoDichThanhToan payment = paymentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch với ID: " + id));

        if ("THANH_CONG".equals(payment.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Không được phép xóa lịch sử giao dịch đã thanh toán thành công");
        }

        payment.markDeleted();
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public GiaoDichThanhToanResponse simulateSuccess(Long orderId, String method) {
        DonHang order = donHangRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId));

        if ("DA_THANH_TOAN".equals(order.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng này đã ở trạng thái ĐÃ THANH TOÁN");
        }
        if ("DA_HUY".equals(order.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng đã bị hủy, không thể thanh toán");
        }

        String paymentMethod = method != null && !method.isBlank() ? method.trim().toUpperCase() : "MOMO";

        if ("MOMO".equals(paymentMethod)) {
            List<GiaoDichThanhToan> existingPayments = paymentRepository.findAllByDonHangIdAndDeletedAtIsNull(orderId);
            String txnRef = existingPayments.stream()
                    .filter(p -> "MOMO".equalsIgnoreCase(p.getPhuongThucThanhToan()))
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .map(GiaoDichThanhToan::getNoiDungVietqr)
                    .filter(s -> s != null && !s.isBlank())
                    .findFirst()
                    .orElse(orderId + "_" + System.currentTimeMillis());

            String amount = String.valueOf(order.getTongTienThanhToan().longValue());
            String requestId = txnRef + "_req";
            String extraData = "";
            String message = "Successful.";
            String orderInfo = "Thanh toan don hang #" + orderId;
            String orderType = "momo_wallet";
            String partnerCode = momoProperties.getPartnerCode();
            String payType = "qr";
            String responseTime = String.valueOf(System.currentTimeMillis());
            String resultCode = "0";
            String transId = "sim_" + System.currentTimeMillis();

            String rawSignature = "accessKey=" + momoProperties.getAccessKey()
                    + "&amount=" + amount
                    + "&extraData=" + extraData
                    + "&message=" + message
                    + "&orderId=" + txnRef
                    + "&orderInfo=" + orderInfo
                    + "&orderType=" + orderType
                    + "&partnerCode=" + partnerCode
                    + "&payType=" + payType
                    + "&requestId=" + requestId
                    + "&responseTime=" + responseTime
                    + "&resultCode=" + resultCode
                    + "&transId=" + transId;

            String signature = MomoPaymentGateway.hmacSHA256(momoProperties.getSecretKey(), rawSignature);

            Map<String, String> params = new HashMap<>();
            params.put("partnerCode", partnerCode);
            params.put("orderId", txnRef);
            params.put("requestId", requestId);
            params.put("amount", amount);
            params.put("orderInfo", orderInfo);
            params.put("orderType", orderType);
            params.put("transId", transId);
            params.put("resultCode", resultCode);
            params.put("message", message);
            params.put("payType", payType);
            params.put("responseTime", responseTime);
            params.put("extraData", extraData);
            params.put("signature", signature);

            return handleCallback("MOMO", params);
        } else if ("VNPAY".equals(paymentMethod)) {
            List<GiaoDichThanhToan> existingPayments = paymentRepository.findAllByDonHangIdAndDeletedAtIsNull(orderId);
            String txnRef = existingPayments.stream()
                    .filter(p -> "VNPAY".equalsIgnoreCase(p.getPhuongThucThanhToan()))
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .map(GiaoDichThanhToan::getNoiDungVietqr)
                    .filter(s -> s != null && !s.isBlank())
                    .findFirst()
                    .orElse(orderId + "_" + System.currentTimeMillis());

            String amount = String.valueOf(order.getTongTienThanhToan().multiply(BigDecimal.valueOf(100)).longValue());

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Amount", amount);
            vnpParams.put("vnp_BankCode", "NCB");
            vnpParams.put("vnp_BankTranNo", "VNP" + System.currentTimeMillis());
            vnpParams.put("vnp_CardType", "ATM");
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + orderId);
            vnpParams.put("vnp_PayDate", DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh")).format(Instant.now()));
            vnpParams.put("vnp_ResponseCode", "00");
            vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());
            vnpParams.put("vnp_TransactionNo", "sim_" + System.currentTimeMillis());
            vnpParams.put("vnp_TransactionStatus", "00");
            vnpParams.put("vnp_TxnRef", txnRef);

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            try {
                for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
                    String field = itr.next();
                    String value = vnpParams.get(field);
                    hashData.append(field).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Lỗi mã hóa dữ liệu mô phỏng VNPay", ex);
            }

            String vnpSecureHash = VnPayPaymentGateway.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
            vnpParams.put("vnp_SecureHash", vnpSecureHash);

            return handleCallback("VNPAY", vnpParams);
        } else if ("CHUYEN_KHOAN_QR".equals(paymentMethod) || "VIETQR".equals(paymentMethod)) {
            List<GiaoDichThanhToan> existingPayments = paymentRepository.findAllByDonHangIdAndDeletedAtIsNull(orderId);
            String txnRef = existingPayments.stream()
                    .filter(p -> "CHUYEN_KHOAN_QR".equalsIgnoreCase(p.getPhuongThucThanhToan()))
                    .sorted((a, b) -> b.getId().compareTo(a.getId()))
                    .map(GiaoDichThanhToan::getNoiDungVietqr)
                    .filter(s -> s != null && !s.isBlank())
                    .findFirst()
                    .orElse(orderId + "_" + System.currentTimeMillis());

            Map<String, String> params = new HashMap<>();
            params.put("secretToken", vietQrProperties.getWebhookSecret());
            params.put("transactionReference", txnRef);
            params.put("amount", String.valueOf(order.getTongTienThanhToan().longValue()));
            params.put("content", "DH" + orderId + " SIMULATE VIETQR TRANSFER");

            return handleCallback("CHUYEN_KHOAN_QR", params);
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Phương thức mô phỏng không hợp lệ. Chỉ hỗ trợ MOMO, VNPAY hoặc CHUYEN_KHOAN_QR");
        }
    }
}
