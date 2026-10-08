package com.milktea.promotion.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.catalog.entity.LoaiSanPham;
import com.milktea.catalog.entity.SanPham;
import com.milktea.catalog.repository.LoaiSanPhamRepository;
import com.milktea.catalog.repository.SanPhamRepository;
import com.milktea.common.exception.BusinessException;
import com.milktea.promotion.dto.KhuyenMaiSanPhamRequest;
import com.milktea.promotion.dto.KhuyenMaiSanPhamResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.entity.KhuyenMaiSanPham;
import com.milktea.promotion.entity.LoaiGiamGia;
import com.milktea.promotion.mapper.KhuyenMaiSanPhamMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiSanPhamRepository;
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
class KhuyenMaiSanPhamServiceImplTest {

    @Mock
    private KhuyenMaiSanPhamRepository repository;

    @Mock
    private KhuyenMaiSanPhamMapper mapper;

    @Mock
    private ChuongTrinhKhuyenMaiRepository chuongTrinhRepository;

    @Mock
    private SanPhamRepository sanPhamRepository;

    @Mock
    private LoaiSanPhamRepository loaiSanPhamRepository;

    @InjectMocks
    private KhuyenMaiSanPhamServiceImpl service;

    private ChuongTrinhKhuyenMai program;
    private SanPham product;
    private KhuyenMaiSanPham sampleKM;

    @BeforeEach
    void setUp() {
        program = new ChuongTrinhKhuyenMai();
        program.setId(1L);
        program.setTenChuongTrinh("KM Trà Sữa");

        product = new SanPham();
        product.setId(100L);
        product.setTenSanPham("Trà sữa Oolong");

        sampleKM = new KhuyenMaiSanPham();
        sampleKM.setId(5L);
        sampleKM.setChuongTrinh(program);
        sampleKM.setSanPham(product);
        sampleKM.setLoaiGiamGia(LoaiGiamGia.PHAN_TRAM);
        sampleKM.setGiaTriGiam(BigDecimal.valueOf(15));
    }

    @Test
    @DisplayName("Tạo khuyến mãi sản phẩm thành công")
    void testCreate_Success() {
        KhuyenMaiSanPhamRequest request = new KhuyenMaiSanPhamRequest(
                1L, 100L, null, LoaiGiamGia.PHAN_TRAM, BigDecimal.valueOf(15)
        );

        when(chuongTrinhRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(program));
        when(sanPhamRepository.findById(100L)).thenReturn(Optional.of(product));
        when(mapper.toEntity(request)).thenReturn(sampleKM);
        when(repository.save(sampleKM)).thenReturn(sampleKM);
        when(mapper.toResponse(sampleKM)).thenReturn(
                new KhuyenMaiSanPhamResponse(5L, 1L, 100L, null, LoaiGiamGia.PHAN_TRAM, BigDecimal.valueOf(15))
        );

        KhuyenMaiSanPhamResponse response = service.create(request);
        assertNotNull(response);
        assertEquals(5L, response.id());
        verify(repository, times(1)).save(sampleKM);
    }

    @Test
    @DisplayName("Tạo khuyến mãi thất bại khi phần trăm vượt quá 100%")
    void testCreate_PercentageExceeds100_ThrowsBadRequest() {
        KhuyenMaiSanPhamRequest request = new KhuyenMaiSanPhamRequest(
                1L, 100L, null, LoaiGiamGia.PHAN_TRAM, BigDecimal.valueOf(120)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("không được vượt quá 100%"));
    }

    @Test
    @DisplayName("Tạo khuyến mãi thất bại khi không cung cấp cả sản phẩm lẫn danh mục")
    void testCreate_MissingProductAndCategory_ThrowsBadRequest() {
        KhuyenMaiSanPhamRequest request = new KhuyenMaiSanPhamRequest(
                1L, null, null, LoaiGiamGia.PHAN_TRAM, BigDecimal.valueOf(20)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Phải chọn ít nhất một sản phẩm hoặc một danh mục"));
    }

    @Test
    @DisplayName("Xóa khuyến mãi sản phẩm thành công (xóa mềm)")
    void testDelete_Success_SoftDeleted() {
        when(repository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(sampleKM));

        service.delete(5L);

        assertNotNull(sampleKM.getDeletedAt());
        verify(repository, times(1)).save(sampleKM);
    }
}
