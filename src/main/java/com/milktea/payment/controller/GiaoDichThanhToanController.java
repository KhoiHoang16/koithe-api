package com.milktea.payment.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.payment.dto.*;
import com.milktea.payment.service.GiaoDichThanhToanService;
import com.milktea.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment")
public class GiaoDichThanhToanController {
    private final GiaoDichThanhToanService service;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public GiaoDichThanhToanController(GiaoDichThanhToanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List payments")
    public ApiResponse<List<GiaoDichThanhToanResponse>> list() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment")
    public ApiResponse<GiaoDichThanhToanResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create payment")
    public ApiResponse<GiaoDichThanhToanResponse> create(@Valid @RequestBody GiaoDichThanhToanRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PostMapping("/guest-cash")
    @Operation(summary = "Create a COD payment for a guest order")
    public ApiResponse<GiaoDichThanhToanResponse> createGuestCash(
            @Valid @RequestBody GuestCashPaymentRequest request) {
        return ApiResponse.success(service.createGuestCash(request));
    }

    @PostMapping("/{id}/confirm-cash")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "Thu ngân xác nhận đã thu tiền mặt cho giao dịch")
    public ApiResponse<GiaoDichThanhToanResponse> confirmCash(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.confirmCash(id, principal));
    }

    @PostMapping("/confirm-cash")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "Thu ngân xác nhận đã thu tiền mặt theo mã đơn hàng")
    public ApiResponse<GiaoDichThanhToanResponse> confirmCashByOrder(
            @RequestParam Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(service.confirmCashByOrderId(orderId, principal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment")
    public ApiResponse<GiaoDichThanhToanResponse> update(@PathVariable Long id,
            @Valid @RequestBody GiaoDichThanhToanRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/vnpay-return")
    @Operation(summary = "Nhận kết quả điều hướng từ VNPay (Return URL)")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> params) {
        GiaoDichThanhToanResponse response = service.handleCallback("VNPAY", params);
        String redirectUrl = "THANH_CONG".equals(response.trangThai())
                ? frontendUrl + "/order-success/" + response.maDonHang()
                : frontendUrl + "/checkout/payment/callback?orderId=" + response.maDonHang()
                        + (response.id() != null ? "&paymentId=" + response.id() : "")
                        + "&status=" + response.trangThai();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/vnpay-ipn")
    @Operation(summary = "Tiếp nhận webhook IPN từ VNPay (Server-to-Server)")
    public ApiResponse<GiaoDichThanhToanResponse> vnpayIpn(@RequestParam Map<String, String> params) {
        return ApiResponse.success(service.handleCallback("VNPAY", params));
    }

    @GetMapping("/momo-return")
    @Operation(summary = "Nhận kết quả điều hướng từ MoMo (Return URL)")
    public ResponseEntity<Void> momoReturn(@RequestParam Map<String, String> params) {
        GiaoDichThanhToanResponse response = service.handleCallback("MOMO", params);
        String redirectUrl = "THANH_CONG".equals(response.trangThai())
                ? frontendUrl + "/order-success/" + response.maDonHang()
                : frontendUrl + "/checkout/payment/callback?orderId=" + response.maDonHang()
                        + (response.id() != null ? "&paymentId=" + response.id() : "")
                        + "&status=" + response.trangThai();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @PostMapping("/momo-ipn")
    @Operation(summary = "Tiếp nhận webhook IPN từ MoMo (Server-to-Server)")
    public ApiResponse<GiaoDichThanhToanResponse> momoIpn(@RequestBody Map<String, Object> payload) {
        Map<String, String> params = payload.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() != null ? String.valueOf(e.getValue()) : ""
                ));
        return ApiResponse.success(service.handleCallback("MOMO", params));
    }

    @PostMapping("/vietqr-webhook")
    @Operation(summary = "Tiếp nhận webhook biến động số dư chuyển khoản VietQR")
    public ApiResponse<GiaoDichThanhToanResponse> vietQrWebhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String headerSecret,
            @RequestBody Map<String, Object> payload) {
        Map<String, String> params = payload.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() != null ? String.valueOf(e.getValue()) : ""
                ));

        if (headerSecret != null && !headerSecret.isBlank()) {
            params.put("secretToken", headerSecret);
        }

        return ApiResponse.success(service.handleCallback("CHUYEN_KHOAN_QR", params));
    }

    @PostMapping("/dev-simulate-success")
    @Operation(summary = "Mô phỏng thanh toán thành công (Dành riêng cho Dev test MoMo/VNPay trên Swagger)")
    public ApiResponse<GiaoDichThanhToanResponse> devSimulateSuccess(
            @RequestParam Long orderId,
            @RequestParam(defaultValue = "MOMO") String method) {
        return ApiResponse.success(service.simulateSuccess(orderId, method));
    }
}
