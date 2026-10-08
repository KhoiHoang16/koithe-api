package com.milktea.promotion.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiRequest;
import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.mapper.ChuongTrinhKhuyenMaiMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiHoaDonRepository;
import com.milktea.promotion.repository.KhuyenMaiSanPhamRepository;
import com.milktea.promotion.repository.KhuyenMaiVoucherRepository;
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
class ChuongTrinhKhuyenMaiServiceImplTest {

    @Mock
    private ChuongTrinhKhuyenMaiRepository repository;

    @Mock
    private ChuongTrinhKhuyenMaiMapper mapper;

    @Mock
    private KhuyenMaiSanPhamRepository kmSanPhamRepository;

    @Mock
    private KhuyenMaiHoaDonRepository kmHoaDonRepository;

    @Mock
    private KhuyenMaiVoucherRepository kmVoucherRepository;

    @InjectMocks
    private ChuongTrinhKhuyenMaiServiceImpl programService;

    private ChuongTrinhKhuyenMai sampleProgram;

    @BeforeEach
    void setUp() {
        sampleProgram = new ChuongTrinhKhuyenMai();
        sampleProgram.setId(1L);
        sampleProgram.setTenChuongTrinh("Đại tiệc trà sữa");
        sampleProgram.setDangHoatDong(true);
        sampleProgram.setNgayBatDau(Instant.now());
        sampleProgram.setNgayKetThuc(Instant.now().plus(7, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("Tạo chương trình khuyến mãi thành công với ngày hợp lệ")
    void testCreate_Success() {
        ChuongTrinhKhuyenMaiRequest request = new ChuongTrinhKhuyenMaiRequest(
                "Đại tiệc trà sữa", "Mô tả",
                Instant.now(), Instant.now().plus(7, ChronoUnit.DAYS), true
        );

        when(mapper.toEntity(request)).thenReturn(sampleProgram);
        when(repository.save(sampleProgram)).thenReturn(sampleProgram);
        when(mapper.toResponse(sampleProgram)).thenReturn(
                new ChuongTrinhKhuyenMaiResponse(1L, "Đại tiệc trà sữa", "Mô tả",
                        sampleProgram.getNgayBatDau(), sampleProgram.getNgayKetThuc(), true)
        );

        ChuongTrinhKhuyenMaiResponse response = programService.create(request);

        assertNotNull(response);
        assertEquals("Đại tiệc trà sữa", response.tenChuongTrinh());
        verify(repository, times(1)).save(sampleProgram);
    }

    @Test
    @DisplayName("Tạo chương trình thất bại khi ngày bắt đầu sau ngày kết thúc")
    void testCreate_InvalidDateRange_ThrowsBadRequest() {
        Instant now = Instant.now();
        ChuongTrinhKhuyenMaiRequest request = new ChuongTrinhKhuyenMaiRequest(
                "Khuyến mãi sai ngày", "Mô tả",
                now.plus(5, ChronoUnit.DAYS), now, true
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> programService.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa chương trình thất bại khi đang có voucher liên kết (xung đột dữ liệu)")
    void testDelete_WithLinkedVoucher_ThrowsConflict() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sampleProgram));
        when(kmSanPhamRepository.existsByChuongTrinhIdAndDeletedAtIsNull(1L)).thenReturn(false);
        when(kmHoaDonRepository.existsByChuongTrinhIdAndDeletedAtIsNull(1L)).thenReturn(false);
        when(kmVoucherRepository.existsByChuongTrinhIdAndDeletedAtIsNull(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> programService.delete(1L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("voucher liên kết"));
        assertNull(sampleProgram.getDeletedAt());
    }

    @Test
    @DisplayName("Xóa mềm chương trình thành công khi không có dữ liệu con liên kết")
    void testDelete_Success_SoftDeleted() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sampleProgram));
        when(kmSanPhamRepository.existsByChuongTrinhIdAndDeletedAtIsNull(1L)).thenReturn(false);
        when(kmHoaDonRepository.existsByChuongTrinhIdAndDeletedAtIsNull(1L)).thenReturn(false);
        when(kmVoucherRepository.existsByChuongTrinhIdAndDeletedAtIsNull(1L)).thenReturn(false);

        programService.delete(1L);

        assertNotNull(sampleProgram.getDeletedAt());
        verify(repository, times(1)).save(sampleProgram);
    }

    @Test
    @DisplayName("Lấy chi tiết ném lỗi 404 khi không tìm thấy ID")
    void testGetById_NotFound() {
        when(repository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> programService.getById(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
