package com.milktea.promotion.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.promotion.dto.KhuyenMaiVoucherRequest;
import com.milktea.promotion.dto.ValidateVoucherRequest;
import com.milktea.promotion.dto.ValidateVoucherResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.entity.KhuyenMaiVoucher;
import com.milktea.promotion.entity.LoaiGiamGia;
import com.milktea.promotion.mapper.KhuyenMaiVoucherMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiVoucherRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class KhuyenMaiVoucherServiceImplTest {

    @Mock
    private KhuyenMaiVoucherRepository voucherRepository;

    @Mock
    private KhuyenMaiVoucherMapper mapper;

    @Mock
    private ChuongTrinhKhuyenMaiRepository chuongTrinhRepository;

    @InjectMocks
    private KhuyenMaiVoucherServiceImpl voucherService;

    private ChuongTrinhKhuyenMai activeProgram;
    private KhuyenMaiVoucher sampleVoucher;

    @BeforeEach
    void setUp() {
        activeProgram = new ChuongTrinhKhuyenMai();
        activeProgram.setId(1L);
        activeProgram.setTenChuongTrinh("Mùa Hè Sôi Động");
        activeProgram.setDangHoatDong(true);
        activeProgram.setNgayBatDau(Instant.now().minus(2, ChronoUnit.DAYS));
        activeProgram.setNgayKetThuc(Instant.now().plus(5, ChronoUnit.DAYS));

        sampleVoucher = new KhuyenMaiVoucher();
        sampleVoucher.setId(10L);
        sampleVoucher.setChuongTrinh(activeProgram);
        sampleVoucher.setMaCode("SUMMER20");
        sampleVoucher.setLoaiGiamGia(LoaiGiamGia.PHAN_TRAM);
        sampleVoucher.setGiaTriGiam(BigDecimal.valueOf(20)); // 20%
        sampleVoucher.setMucGiamToiDa(BigDecimal.valueOf(30000)); // max 30,000 VND
        sampleVoucher.setDonHangToiThieu(BigDecimal.valueOf(50000));
        sampleVoucher.setGioiHanSuDung(100);
        sampleVoucher.setSoLuotDaDung(10);
    }

    @Test
    @DisplayName("Validate Voucher thành công với giảm theo % và áp trần mức giảm tối đa")
    void testValidateVoucher_Success_PercentageWithCap() {
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));

        // Đơn hàng 200,000 VND -> 20% là 40,000 VND, vượt trần 30,000 -> Giảm 30,000 VND
        ValidateVoucherRequest request = new ValidateVoucherRequest("summer20", BigDecimal.valueOf(200000));
        ValidateVoucherResponse response = voucherService.validateVoucher(request);

        assertTrue(response.hopLe());
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(response.soTienGiam()));
        assertEquals(0, BigDecimal.valueOf(170000).compareTo(response.tongTienSauGiam()));
        assertEquals("SUMMER20", response.maCode());
    }

    @Test
    @DisplayName("Validate Voucher thành công với giảm số tiền cố định")
    void testValidateVoucher_Success_FixedAmount() {
        sampleVoucher.setLoaiGiamGia(LoaiGiamGia.TIEN_CO_DINH);
        sampleVoucher.setGiaTriGiam(BigDecimal.valueOf(25000));
        sampleVoucher.setMucGiamToiDa(null);

        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SUMMER20", BigDecimal.valueOf(100000));
        ValidateVoucherResponse response = voucherService.validateVoucher(request);

        assertTrue(response.hopLe());
        assertEquals(0, BigDecimal.valueOf(25000).compareTo(response.soTienGiam()));
        assertEquals(0, BigDecimal.valueOf(75000).compareTo(response.tongTienSauGiam()));
    }

    @Test
    @DisplayName("Validate Voucher không vượt quá giá trị đơn hàng (không để âm tiền)")
    void testValidateVoucher_FixedAmount_CannotExceedOrderTotal() {
        sampleVoucher.setLoaiGiamGia(LoaiGiamGia.TIEN_CO_DINH);
        sampleVoucher.setGiaTriGiam(BigDecimal.valueOf(100000));
        sampleVoucher.setDonHangToiThieu(BigDecimal.ZERO);

        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SUMMER20", BigDecimal.valueOf(60000));
        ValidateVoucherResponse response = voucherService.validateVoucher(request);

        assertTrue(response.hopLe());
        assertEquals(0, BigDecimal.valueOf(60000).compareTo(response.soTienGiam()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.tongTienSauGiam()));
    }

    @Test
    @DisplayName("Validate Voucher thất bại khi mã không tồn tại")
    void testValidateVoucher_NotFound() {
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("UNKNOWN"))
                .thenReturn(Optional.empty());

        ValidateVoucherRequest request = new ValidateVoucherRequest("UNKNOWN", BigDecimal.valueOf(100000));
        ValidateVoucherResponse response = voucherService.validateVoucher(request);

        assertFalse(response.hopLe());
        assertEquals("Mã voucher không tồn tại", response.thongDiep());
    }

    @Test
    @DisplayName("Validate Voucher thất bại khi chương trình khuyến mãi bị tắt hoặc hết hạn")
    void testValidateVoucher_ProgramInactiveOrExpired() {
        activeProgram.setDangHoatDong(false);
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SUMMER20", BigDecimal.valueOf(100000));
        ValidateVoucherResponse response = voucherService.validateVoucher(request);

        assertFalse(response.hopLe());
        assertEquals("Chương trình khuyến mãi của voucher đang không hoạt động", response.thongDiep());
    }

    @Test
    @DisplayName("Validate Voucher thất bại khi đã hết quota lượt sử dụng")
    void testValidateVoucher_QuotaExceeded() {
        sampleVoucher.setGioiHanSuDung(50);
        sampleVoucher.setSoLuotDaDung(50);
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SUMMER20", BigDecimal.valueOf(100000));
        ValidateVoucherResponse response = voucherService.validateVoucher(request);

        assertFalse(response.hopLe());
        assertEquals("Voucher đã hết lượt sử dụng", response.thongDiep());
    }

    @Test
    @DisplayName("Validate Voucher thất bại khi đơn hàng chưa đạt giá trị tối thiểu")
    void testValidateVoucher_MinOrderNotMet() {
        sampleVoucher.setDonHangToiThieu(BigDecimal.valueOf(150000));
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));

        ValidateVoucherRequest request = new ValidateVoucherRequest("SUMMER20", BigDecimal.valueOf(100000));
        ValidateVoucherResponse response = voucherService.validateVoucher(request);

        assertFalse(response.hopLe());
        assertTrue(response.thongDiep().contains("Đơn hàng chưa đạt giá trị tối thiểu"));
    }

    @Test
    @DisplayName("Apply Voucher thành công và gọi atomic update tăng lượt dùng")
    void testApplyVoucher_Success() {
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));
        when(voucherRepository.tangSoLuotDaDungAtomic(10L)).thenReturn(1);

        ValidateVoucherRequest request = new ValidateVoucherRequest("SUMMER20", BigDecimal.valueOf(100000));
        ValidateVoucherResponse response = voucherService.applyVoucher(request);

        assertTrue(response.hopLe());
        verify(voucherRepository, times(1)).tangSoLuotDaDungAtomic(10L);
    }

    @Test
    @DisplayName("Apply Voucher thất bại khi race condition khiến atomic update trả về 0")
    void testApplyVoucher_RaceConditionQuotaExhausted() {
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));
        when(voucherRepository.tangSoLuotDaDungAtomic(10L)).thenReturn(0);

        ValidateVoucherRequest request = new ValidateVoucherRequest("SUMMER20", BigDecimal.valueOf(100000));

        BusinessException ex = assertThrows(BusinessException.class, () -> voucherService.applyVoucher(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Voucher đã hết lượt sử dụng", ex.getMessage());
    }

    @Test
    @DisplayName("Release Voucher gọi atomic update giảm lượt dùng")
    void testReleaseVoucher_CallsAtomicDecrement() {
        when(voucherRepository.findByMaCodeIgnoreCaseAndDeletedAtIsNull("SUMMER20"))
                .thenReturn(Optional.of(sampleVoucher));

        voucherService.releaseVoucher("summer20");

        verify(voucherRepository, times(1)).giamSoLuotDaDungAtomic(10L);
    }

    @Test
    @DisplayName("Tạo mới Voucher chuẩn hóa mã UPPERCASE và kiểm tra trùng mã")
    void testCreateVoucher_CodeNormalizedAndDuplicateCheck() {
        when(voucherRepository.existsByMaCodeIgnoreCaseAndDeletedAtIsNull("NEWCODE")).thenReturn(true);

        KhuyenMaiVoucherRequest request = new KhuyenMaiVoucherRequest(
                1L, " newcode ", LoaiGiamGia.TIEN_CO_DINH, BigDecimal.valueOf(10000),
                null, BigDecimal.ZERO, 100, 0
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> voucherService.create(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("NEWCODE"));
    }
}
