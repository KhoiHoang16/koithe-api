package com.milktea.payment.controller;

import com.milktea.common.response.ApiResponse;
import com.milktea.payment.dto.*;
import com.milktea.payment.service.GiaoDichThanhToanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment")
public class GiaoDichThanhToanController {
    private final GiaoDichThanhToanService service;

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
    public ApiResponse<GiaoDichThanhToanResponse> vnpayReturn(@RequestParam java.util.Map<String, String> params) {
        return ApiResponse.success(service.handleCallback("VNPAY", params));
    }

    @GetMapping("/vnpay-ipn")
    @Operation(summary = "Tiếp nhận webhook IPN từ VNPay (Server-to-Server)")
    public ApiResponse<GiaoDichThanhToanResponse> vnpayIpn(@RequestParam java.util.Map<String, String> params) {
        return ApiResponse.success(service.handleCallback("VNPAY", params));
    }

    @GetMapping("/momo-return")
    @Operation(summary = "Nhận kết quả điều hướng từ MoMo (Return URL)")
    public ApiResponse<GiaoDichThanhToanResponse> momoReturn(@RequestParam java.util.Map<String, String> params) {
        return ApiResponse.success(service.handleCallback("MOMO", params));
    }

    @PostMapping("/momo-ipn")
    @Operation(summary = "Tiếp nhận webhook IPN từ MoMo (Server-to-Server)")
    public ApiResponse<GiaoDichThanhToanResponse> momoIpn(@RequestBody java.util.Map<String, Object> payload) {
        java.util.Map<String, String> params = payload.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        e -> e.getValue() != null ? String.valueOf(e.getValue()) : ""
                ));
        return ApiResponse.success(service.handleCallback("MOMO", params));
    }

    @PostMapping("/vietqr-webhook")
    @Operation(summary = "Tiếp nhận webhook biến động số dư chuyển khoản VietQR")
    public ApiResponse<GiaoDichThanhToanResponse> vietQrWebhook(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String headerSecret,
            @RequestBody java.util.Map<String, Object> payload) {
        java.util.Map<String, String> params = payload.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        java.util.Map.Entry::getKey,
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
