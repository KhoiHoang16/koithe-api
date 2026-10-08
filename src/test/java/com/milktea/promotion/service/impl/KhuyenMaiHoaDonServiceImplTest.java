package com.milktea.promotion.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.promotion.dto.KhuyenMaiHoaDonRequest;
import com.milktea.promotion.dto.KhuyenMaiHoaDonResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.entity.KhuyenMaiHoaDon;
import com.milktea.promotion.entity.LoaiGiamGia;
import com.milktea.promotion.mapper.KhuyenMaiHoaDonMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiHoaDonRepository;
import java.math.BigDecimal;
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
class KhuyenMaiHoaDonServiceImplTest {

    @Mock
    private KhuyenMaiHoaDonRepository repository;

    @Mock
    private KhuyenMaiHoaDonMapper mapper;

    @Mock
    private ChuongTrinhKhuyenMaiRepository chuongTrinhRepository;

    @InjectMocks
    private KhuyenMaiHoaDonServiceImpl service;

    private ChuongTrinhKhuyenMai program;
    private KhuyenMaiHoaDon sampleKM;

    @BeforeEach
    void setUp() {
        program = new ChuongTrinhKhuyenMai();
        program.setId(1L);
        program.setTenChuongTrinh("KM Cuối Tuần");

        sampleKM = new KhuyenMaiHoaDon();
        sampleKM.setId(20L);
        sampleKM.setChuongTrinh(program);
        sampleKM.setDonHangToiThieu(BigDecimal.valueOf(100000));
        sampleKM.setLoaiGiamGia(LoaiGiamGia.PHAN_TRAM);
        sampleKM.setGiaTriGiam(BigDecimal.valueOf(10));
        sampleKM.setMucGiamToiDa(BigDecimal.valueOf(50000));
    }

    @Test
    @DisplayName("Tạo khuyến mãi hóa đơn thành công")
    void testCreate_Success() {
        KhuyenMaiHoaDonRequest request = new KhuyenMaiHoaDonRequest(
                1L, BigDecimal.valueOf(100000), LoaiGiamGia.PHAN_TRAM,
                BigDecimal.valueOf(10), BigDecimal.valueOf(50000)
        );

        when(chuongTrinhRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(program));
        when(mapper.toEntity(request)).thenReturn(sampleKM);
        when(repository.save(sampleKM)).thenReturn(sampleKM);
        when(mapper.toResponse(sampleKM)).thenReturn(
                new KhuyenMaiHoaDonResponse(20L, 1L, BigDecimal.valueOf(100000), LoaiGiamGia.PHAN_TRAM,
                        BigDecimal.valueOf(10), BigDecimal.valueOf(50000))
        );

        KhuyenMaiHoaDonResponse response = service.create(request);
        assertNotNull(response);
        assertEquals(20L, response.id());
        verify(repository, times(1)).save(sampleKM);
    }

    @Test
    @DisplayName("Tạo khuyến mãi hóa đơn thất bại khi đơn tối thiểu âm")
    void testCreate_NegativeMinOrder_ThrowsBadRequest() {
        KhuyenMaiHoaDonRequest request = new KhuyenMaiHoaDonRequest(
                1L, BigDecimal.valueOf(-1000), LoaiGiamGia.PHAN_TRAM,
                BigDecimal.valueOf(10), BigDecimal.valueOf(50000)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Đơn hàng tối thiểu không được âm"));
    }

    @Test
    @DisplayName("Tạo khuyến mãi hóa đơn thất bại khi phần trăm vượt 100%")
    void testCreate_PercentageExceeds100_ThrowsBadRequest() {
        KhuyenMaiHoaDonRequest request = new KhuyenMaiHoaDonRequest(
                1L, BigDecimal.valueOf(100000), LoaiGiamGia.PHAN_TRAM,
                BigDecimal.valueOf(105), BigDecimal.valueOf(50000)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("không được vượt quá 100%"));
    }

    @Test
    @DisplayName("Xóa khuyến mãi hóa đơn thành công (xóa mềm)")
    void testDelete_Success_SoftDeleted() {
        when(repository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(sampleKM));

        service.delete(20L);

        assertNotNull(sampleKM.getDeletedAt());
        verify(repository, times(1)).save(sampleKM);
    }
}
