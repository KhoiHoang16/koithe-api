package com.milktea.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.catalog.entity.BienTheSanPham;
import com.milktea.catalog.entity.SanPham;
import com.milktea.catalog.entity.Topping;
import com.milktea.catalog.repository.BienTheSanPhamRepository;
import com.milktea.catalog.repository.ToppingRepository;
import com.milktea.common.exception.BusinessException;
import com.milktea.inventory.dto.ChiTietPhieuNhapRequest;
import com.milktea.inventory.dto.ChiTietPhieuNhapResponse;
import com.milktea.inventory.entity.ChiTietPhieuNhap;
import com.milktea.inventory.entity.PhieuNhapHang;
import com.milktea.inventory.mapper.ChiTietPhieuNhapMapper;
import com.milktea.inventory.repository.ChiTietPhieuNhapRepository;
import com.milktea.inventory.repository.PhieuNhapHangRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChiTietPhieuNhapServiceImplTest {

    @Mock ChiTietPhieuNhapRepository repository;
    @Mock PhieuNhapHangRepository phieuNhapHangRepository;
    @Mock BienTheSanPhamRepository bienTheRepository;
    @Mock ToppingRepository toppingRepository;
    @Mock ChiTietPhieuNhapMapper mapper;

    ChiTietPhieuNhapServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChiTietPhieuNhapServiceImpl(repository, phieuNhapHangRepository, bienTheRepository, toppingRepository, mapper);
    }

    @Test
    void getAllByMaPhieuNhap_returnsPagedLines() {
        ChiTietPhieuNhap line = dummyLine(1L, new BigDecimal("10"), new BigDecimal("15000"));
        when(repository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(line)));
        when(mapper.toResponse(line)).thenReturn(new ChiTietPhieuNhapResponse(1L, "Trà sữa", "Ly", new BigDecimal("10"), new BigDecimal("15000"), new BigDecimal("150000")));

        var res = service.getAllByMaPhieuNhap(1L, 0, 10);

        assertEquals(1, res.content().size());
    }

    @Test
    void create_successWithVariant() {
        PhieuNhapHang phieu = dummyPo(1L, "CHO_DUYET");
        BienTheSanPham bt = dummyVariant(10L, "Trà sữa Trân châu", "M");
        ChiTietPhieuNhapRequest req = new ChiTietPhieuNhapRequest(1L, 10L, null, null, "Ly", new BigDecimal("5"), new BigDecimal("20000"), null);

        when(phieuNhapHangRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(phieu));
        when(bienTheRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(bt));
        when(repository.save(any(ChiTietPhieuNhap.class))).thenAnswer(call -> {
            ChiTietPhieuNhap l = call.getArgument(0);
            l.setId(100L);
            return l;
        });
        when(repository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(1L)).thenReturn(List.of(dummyLine(100L, new BigDecimal("5"), new BigDecimal("20000"))));
        when(mapper.toResponse(any(ChiTietPhieuNhap.class))).thenReturn(new ChiTietPhieuNhapResponse(100L, "Trà sữa Trân châu (M)", "Ly", new BigDecimal("5"), new BigDecimal("20000"), new BigDecimal("100000")));

        ChiTietPhieuNhapResponse resp = service.create(req);

        assertNotNull(resp);
        verify(repository).save(any(ChiTietPhieuNhap.class));
        verify(phieuNhapHangRepository).save(argThat(p -> p.getTongTien().compareTo(new BigDecimal("100000")) == 0));
    }

    @Test
    void create_throwsWhenPoIsNotChoDuyet() {
        PhieuNhapHang phieu = dummyPo(1L, "DA_NHAP_KHO");
        ChiTietPhieuNhapRequest req = new ChiTietPhieuNhapRequest(1L, 10L, null, "Trà", "Ly", new BigDecimal("5"), new BigDecimal("20000"), null);

        when(phieuNhapHangRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(phieu));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void create_throwsWhenQuantityInvalid() {
        PhieuNhapHang phieu = dummyPo(1L, "CHO_DUYET");
        ChiTietPhieuNhapRequest req = new ChiTietPhieuNhapRequest(1L, 10L, null, "Trà", "Ly", BigDecimal.ZERO, new BigDecimal("20000"), null);

        when(phieuNhapHangRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(phieu));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void create_throwsWhenBothVariantAndToppingNull() {
        PhieuNhapHang phieu = dummyPo(1L, "CHO_DUYET");
        ChiTietPhieuNhapRequest req = new ChiTietPhieuNhapRequest(1L, null, null, "Trà", "Ly", new BigDecimal("5"), new BigDecimal("20000"), null);

        when(phieuNhapHangRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(phieu));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void update_success() {
        PhieuNhapHang phieu = dummyPo(1L, "CHO_DUYET");
        ChiTietPhieuNhap line = dummyLine(100L, new BigDecimal("5"), new BigDecimal("20000"));
        line.setPhieuNhapHang(phieu);

        ChiTietPhieuNhapRequest req = new ChiTietPhieuNhapRequest(null, null, null, null, null, new BigDecimal("10"), new BigDecimal("25000"), null);

        when(repository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(line));
        when(repository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(1L)).thenReturn(List.of(line));
        when(mapper.toResponse(line)).thenReturn(new ChiTietPhieuNhapResponse(100L, "Trà sữa", "Ly", new BigDecimal("10"), new BigDecimal("25000"), new BigDecimal("250000")));

        service.update(100L, req);

        assertEquals(new BigDecimal("10"), line.getSoLuong());
        assertEquals(new BigDecimal("25000"), line.getDonGiaNhap());
        assertEquals(new BigDecimal("250000"), line.getThanhTien());
    }

    @Test
    void update_throwsWhenPoNotChoDuyet() {
        PhieuNhapHang phieu = dummyPo(1L, "DA_NHAP_KHO");
        ChiTietPhieuNhap line = dummyLine(100L, new BigDecimal("5"), new BigDecimal("20000"));
        line.setPhieuNhapHang(phieu);

        ChiTietPhieuNhapRequest req = new ChiTietPhieuNhapRequest(null, null, null, null, null, new BigDecimal("10"), null, null);

        when(repository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(line));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(100L, req));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void delete_successAndRecalculatesTotal() {
        PhieuNhapHang phieu = dummyPo(1L, "CHO_DUYET");
        ChiTietPhieuNhap line = dummyLine(100L, new BigDecimal("5"), new BigDecimal("20000"));
        line.setPhieuNhapHang(phieu);

        when(repository.findByIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(line));
        when(repository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

        service.delete(100L);

        assertNotNull(line.getDeletedAt());
        verify(phieuNhapHangRepository).save(argThat(p -> p.getTongTien().compareTo(BigDecimal.ZERO) == 0));
    }

    private PhieuNhapHang dummyPo(Long id, String status) {
        PhieuNhapHang p = new PhieuNhapHang();
        p.setId(id);
        p.setTrangThai(status);
        p.setTongTien(BigDecimal.ZERO);
        return p;
    }

    private ChiTietPhieuNhap dummyLine(Long id, BigDecimal qty, BigDecimal price) {
        ChiTietPhieuNhap l = new ChiTietPhieuNhap();
        l.setId(id);
        l.setSoLuong(qty);
        l.setDonGiaNhap(price);
        l.setThanhTien(qty.multiply(price));
        return l;
    }

    private BienTheSanPham dummyVariant(Long id, String spName, String size) {
        SanPham sp = new SanPham();
        sp.setTenSanPham(spName);
        BienTheSanPham bt = new BienTheSanPham();
        bt.setId(id);
        bt.setSanPham(sp);
        bt.setKichCo(size);
        return bt;
    }
}
