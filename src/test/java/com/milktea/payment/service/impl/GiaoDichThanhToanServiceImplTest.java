package com.milktea.payment.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.order.entity.DonHang;
import com.milktea.order.repository.DonHangRepository;
import com.milktea.order.service.OrderService;
import com.milktea.payment.dto.GiaoDichThanhToanRequest;
import com.milktea.payment.dto.GiaoDichThanhToanResponse;
import com.milktea.payment.entity.GiaoDichThanhToan;
import com.milktea.payment.gateway.PaymentGatewayFactory;
import com.milktea.payment.gateway.impl.CashPaymentGateway;
import com.milktea.payment.mapper.GiaoDichThanhToanMapper;
import com.milktea.payment.repository.GiaoDichThanhToanRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GiaoDichThanhToanServiceImplTest {
    PaymentGatewayFactory gatewayFactory = mock(PaymentGatewayFactory.class);
    GiaoDichThanhToanRepository paymentRepository = mock(GiaoDichThanhToanRepository.class);
    DonHangRepository donHangRepository = mock(DonHangRepository.class);
    OrderService orderService = mock(OrderService.class);
    GiaoDichThanhToanMapper paymentMapper = mock(GiaoDichThanhToanMapper.class);
    com.milktea.payment.gateway.VnPayProperties vnPayProperties = mock(com.milktea.payment.gateway.VnPayProperties.class);
    com.milktea.payment.gateway.MomoProperties momoProperties = mock(com.milktea.payment.gateway.MomoProperties.class);
    com.milktea.payment.gateway.VietQrProperties vietQrProperties = mock(com.milktea.payment.gateway.VietQrProperties.class);

    GiaoDichThanhToanServiceImpl service = new GiaoDichThanhToanServiceImpl(
            gatewayFactory, paymentRepository, donHangRepository, orderService, paymentMapper,
            vnPayProperties, momoProperties, vietQrProperties);

    CashPaymentGateway cashGateway = new CashPaymentGateway();

    @BeforeEach
    void setUp() {
        when(momoProperties.getPartnerCode()).thenReturn("MOMOBKUN20180529");
        when(momoProperties.getAccessKey()).thenReturn("klm05TvNBzhg7h7j");
        when(momoProperties.getSecretKey()).thenReturn("at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa");
        when(vnPayProperties.getTmnCode()).thenReturn("RFKTEMYR");
        when(vnPayProperties.getHashSecret()).thenReturn("DCEYGTRMORVOTLATHGYIKUJQKRKIRFHU");
        when(vietQrProperties.getWebhookSecret()).thenReturn("test_secret_123");

        when(gatewayFactory.getGateway("TIEN_MAT")).thenReturn(cashGateway);
        when(paymentRepository.save(any(GiaoDichThanhToan.class))).thenAnswer(invocation -> {
            GiaoDichThanhToan p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(paymentMapper.toResponse(any(GiaoDichThanhToan.class))).thenAnswer(invocation -> {
            GiaoDichThanhToan p = invocation.getArgument(0);
            return new GiaoDichThanhToanResponse(
                    p.getId(),
                    p.getDonHang() != null ? p.getDonHang().getId() : null,
                    p.getPhuongThucThanhToan(),
                    p.getSoTien(),
                    p.getNoiDungVietqr(),
                    p.getTrangThai(),
                    p.getThoiGianThanhToan());
        });
    }

    @Test
    void createCashPayment_Success() {
        DonHang order = new DonHang();
        order.setId(10L);
        order.setMaHienThiDon("DH-001");
        order.setTongTienThanhToan(new BigDecimal("45000.00"));
        order.setTrangThai("CHO_XAC_NHAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(order));

        GiaoDichThanhToanRequest request = new GiaoDichThanhToanRequest(
                10L, "TIEN_MAT", new BigDecimal("45000.00"), null, null, null);

        GiaoDichThanhToanResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(10L, response.maDonHang());
        assertEquals("TIEN_MAT", response.phuongThucThanhToan());
        assertEquals(new BigDecimal("45000.00"), response.soTien());
        assertEquals("THANH_CONG", response.trangThai());

        verify(orderService).updateStatus(10L, "DA_THANH_TOAN");
        verify(paymentRepository).save(any(GiaoDichThanhToan.class));
    }

    @Test
    void createCashPayment_FreeOrder_Success() {
        DonHang order = new DonHang();
        order.setId(20L);
        order.setMaHienThiDon("DH-FREE");
        order.setTongTienThanhToan(BigDecimal.ZERO);
        order.setTrangThai("CHO_XAC_NHAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(order));

        GiaoDichThanhToanRequest request = new GiaoDichThanhToanRequest(
                20L, "TIEN_MAT", BigDecimal.ZERO, null, null, null);

        GiaoDichThanhToanResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("THANH_CONG", response.trangThai());
        assertEquals(BigDecimal.ZERO, response.soTien());
        verify(orderService).updateStatus(20L, "DA_THANH_TOAN");
    }

    @Test
    void createCashPayment_OrderNotFound_Throws404() {
        when(donHangRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        GiaoDichThanhToanRequest request = new GiaoDichThanhToanRequest(
                999L, "TIEN_MAT", new BigDecimal("50000"), null, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void createCashPayment_AlreadyPaid_Throws400() {
        DonHang order = new DonHang();
        order.setId(10L);
        order.setTongTienThanhToan(new BigDecimal("50000"));
        order.setTrangThai("DA_THANH_TOAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(order));

        GiaoDichThanhToanRequest request = new GiaoDichThanhToanRequest(
                10L, "TIEN_MAT", new BigDecimal("50000"), null, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("đã được thanh toán"));
    }

    @Test
    void createCashPayment_CancelledOrder_Throws400() {
        DonHang order = new DonHang();
        order.setId(10L);
        order.setTongTienThanhToan(new BigDecimal("50000"));
        order.setTrangThai("DA_HUY");

        when(donHangRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(order));

        GiaoDichThanhToanRequest request = new GiaoDichThanhToanRequest(
                10L, "TIEN_MAT", new BigDecimal("50000"), null, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("đã bị hủy"));
    }

    @Test
    void createCashPayment_WrongAmount_Throws400() {
        DonHang order = new DonHang();
        order.setId(10L);
        order.setTongTienThanhToan(new BigDecimal("50000"));
        order.setTrangThai("CHO_XAC_NHAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(order));

        GiaoDichThanhToanRequest request = new GiaoDichThanhToanRequest(
                10L, "TIEN_MAT", new BigDecimal("30000"), null, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("phải bằng đúng tổng tiền đơn hàng"));
    }

    @Test
    void delete_SuccessfulPayment_Throws400() {
        GiaoDichThanhToan payment = new GiaoDichThanhToan();
        payment.setId(1L);
        payment.setTrangThai("THANH_CONG");

        when(paymentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(payment));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Không được phép xóa"));
    }

    @Test
    void createVnPayPayment_ReturnsPendingAndRedirectUrl() {
        DonHang order = new DonHang();
        order.setId(30L);
        order.setMaHienThiDon("DH-VNPAY");
        order.setTongTienThanhToan(new BigDecimal("60000.00"));
        order.setTrangThai("CHO_XAC_NHAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(order));

        com.milktea.payment.gateway.PaymentGateway vnpayGateway = mock(com.milktea.payment.gateway.PaymentGateway.class);
        when(vnpayGateway.getMethod()).thenReturn("VNPAY");
        when(vnpayGateway.processPayment(any())).thenReturn(
                com.milktea.payment.gateway.dto.GatewayPaymentResult.builder()
                        .success(true)
                        .transactionReference("30_12345")
                        .redirectUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?test=1")
                        .build());
        when(gatewayFactory.getGateway("VNPAY")).thenReturn(vnpayGateway);

        GiaoDichThanhToanRequest request = new GiaoDichThanhToanRequest(
                30L, "VNPAY", new BigDecimal("60000.00"), null, null, null);

        GiaoDichThanhToanResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("CHO_XU_LY", response.trangThai());
        assertEquals("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?test=1", response.redirectUrl());
        verify(orderService, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void handleVnPayCallback_Success_UpdatesOrderToPaid() {
        DonHang order = new DonHang();
        order.setId(30L);
        order.setTongTienThanhToan(new BigDecimal("60000.00"));
        order.setTrangThai("CHO_XAC_NHAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(order));

        com.milktea.payment.gateway.PaymentGateway vnpayGateway = mock(com.milktea.payment.gateway.PaymentGateway.class);
        when(vnpayGateway.handleCallback(any())).thenReturn(
                com.milktea.payment.gateway.dto.GatewayPaymentResult.builder()
                        .success(true)
                        .transactionReference("30_12345")
                        .build());
        when(gatewayFactory.getGateway("VNPAY")).thenReturn(vnpayGateway);

        GiaoDichThanhToan existingPayment = new GiaoDichThanhToan();
        existingPayment.setId(100L);
        existingPayment.setPhuongThucThanhToan("VNPAY");
        existingPayment.setTrangThai("CHO_XU_LY");
        when(paymentRepository.findAllByDonHangIdAndDeletedAtIsNull(30L)).thenReturn(List.of(existingPayment));

        GiaoDichThanhToanResponse response = service.handleCallback("VNPAY", Map.of("vnp_TxnRef", "30_12345"));

        assertNotNull(response);
        assertEquals("THANH_CONG", response.trangThai());
        verify(orderService).updateStatus(30L, "DA_THANH_TOAN");
    }

    @Test
    void simulateSuccess_Momo_Success() {
        DonHang order = new DonHang();
        order.setId(50L);
        order.setTongTienThanhToan(new BigDecimal("50000.00"));
        order.setTrangThai("CHO_XAC_NHAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(order));

        com.milktea.payment.gateway.PaymentGateway momoGateway = mock(com.milktea.payment.gateway.PaymentGateway.class);
        when(momoGateway.handleCallback(any())).thenReturn(
                com.milktea.payment.gateway.dto.GatewayPaymentResult.builder()
                        .success(true)
                        .transactionReference("50_12345")
                        .build());
        when(gatewayFactory.getGateway("MOMO")).thenReturn(momoGateway);

        GiaoDichThanhToanResponse response = service.simulateSuccess(50L, "MOMO");

        assertNotNull(response);
        assertEquals("THANH_CONG", response.trangThai());
        verify(orderService).updateStatus(50L, "DA_THANH_TOAN");
    }

    @Test
    void simulateSuccess_AlreadyPaid_ThrowsException() {
        DonHang order = new DonHang();
        order.setId(51L);
        order.setTrangThai("DA_THANH_TOAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(51L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> service.simulateSuccess(51L, "MOMO"));
    }

    @Test
    void simulateSuccess_VietQr_Success() {
        DonHang order = new DonHang();
        order.setId(52L);
        order.setTongTienThanhToan(new BigDecimal("45000.00"));
        order.setTrangThai("CHO_XAC_NHAN");

        when(donHangRepository.findByIdAndDeletedAtIsNull(52L)).thenReturn(Optional.of(order));

        com.milktea.payment.gateway.PaymentGateway qrGateway = mock(com.milktea.payment.gateway.PaymentGateway.class);
        when(qrGateway.handleCallback(any())).thenReturn(
                com.milktea.payment.gateway.dto.GatewayPaymentResult.builder()
                        .success(true)
                        .transactionReference("52_12345")
                        .build());
        when(gatewayFactory.getGateway("CHUYEN_KHOAN_QR")).thenReturn(qrGateway);

        GiaoDichThanhToanResponse response = service.simulateSuccess(52L, "CHUYEN_KHOAN_QR");

        assertNotNull(response);
        assertEquals("THANH_CONG", response.trangThai());
        verify(orderService).updateStatus(52L, "DA_THANH_TOAN");
    }
}
