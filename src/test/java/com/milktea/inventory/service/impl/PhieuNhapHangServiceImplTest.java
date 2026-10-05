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
import com.milktea.inventory.dto.*;
import com.milktea.inventory.entity.ChiTietPhieuNhap;
import com.milktea.inventory.entity.NhaCungCap;
import com.milktea.inventory.entity.PhieuNhapHang;
import com.milktea.inventory.mapper.PhieuNhapHangMapper;
import com.milktea.inventory.repository.ChiTietPhieuNhapRepository;
import com.milktea.inventory.repository.NhaCungCapRepository;
import com.milktea.inventory.repository.PhieuNhapHangRepository;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.repository.NguoiDungRepository;
import java.math.BigDecimal;
import java.time.Instant;
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
class PhieuNhapHangServiceImplTest {

    @Mock PhieuNhapHangRepository repository;
    @Mock ChiTietPhieuNhapRepository chiTietRepository;
    @Mock NhaCungCapRepository supplierRepository;
    @Mock NguoiDungRepository userRepository;
    @Mock BienTheSanPhamRepository bienTheRepository;
    @Mock ToppingRepository toppingRepository;
    @Mock PhieuNhapHangMapper mapper;

    PhieuNhapHangServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PhieuNhapHangServiceImpl(
                repository, chiTietRepository, supplierRepository, userRepository,
                bienTheRepository, toppingRepository, mapper);
    }

    @Test
    void getAll_returnsPagedList() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "CHO_DUYET");
        when(repository.search(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(po)));
        when(mapper.toResponse(po)).thenReturn(new PhieuNhapHangResponse(1L, "PN-001", BigDecimal.ZERO, null, "CHO_DUYET", Instant.now()));

        var res = service.getAll(null, null, null, null, 0, 10);

        assertEquals(1, res.content().size());
    }

    @Test
    void getAll_throwsWhenFromDateAfterToDate() {
        Instant from = Instant.now();
        Instant to = from.minusSeconds(3600);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getAll(null, from, to, null, 0, 10));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void getById_success() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "CHO_DUYET");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));
        when(mapper.toResponse(po)).thenReturn(new PhieuNhapHangResponse(1L, "PN-001", BigDecimal.ZERO, null, "CHO_DUYET", Instant.now()));

        var res = service.getById(1L);

        assertNotNull(res);
        assertEquals("PN-001", res.maPhieuNhap());
    }

    @Test
    void create_successWithLinesAndCalculatesServerTotal() {
        NhaCungCap ncc = new NhaCungCap();
        ncc.setId(1L);
        ncc.setDangHopTac(true);

        NguoiDung user = new NguoiDung();
        user.setId(2L);
        user.setDangHoatDong(true);

        BienTheSanPham bt = new BienTheSanPham();
        bt.setId(10L);
        SanPham sp = new SanPham();
        sp.setTenSanPham("Trà sữa");
        bt.setSanPham(sp);
        bt.setKichCo("M");

        ChiTietPhieuNhapRequest lineReq = new ChiTietPhieuNhapRequest(null, 10L, null, null, "Ly", new BigDecimal("5"), new BigDecimal("30000"), null);
        PhieuNhapHangRequest req = new PhieuNhapHangRequest(null, 1L, 2L, null, "Ghi chu", null, null, List.of(lineReq));

        when(supplierRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ncc));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bienTheRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(bt));
        when(repository.save(any(PhieuNhapHang.class))).thenAnswer(call -> {
            PhieuNhapHang p = call.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
        when(mapper.toResponse(any(PhieuNhapHang.class))).thenReturn(new PhieuNhapHangResponse(1L, "PN-AUTO", new BigDecimal("150000"), "Ghi chu", "CHO_DUYET", Instant.now()));

        PhieuNhapHangResponse res = service.create(req);

        assertNotNull(res);
        verify(repository, times(2)).save(argThat(p -> {
            assertEquals("CHO_DUYET", p.getTrangThai());
            return true;
        }));
        verify(chiTietRepository).save(argThat(l -> {
            assertEquals(new BigDecimal("150000"), l.getThanhTien());
            return true;
        }));
    }

    @Test
    void create_throwsWhenSupplierNotFound() {
        PhieuNhapHangRequest req = new PhieuNhapHangRequest(null, 99L, 2L, null, null, null, null, null);
        when(supplierRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));
        assertEquals(404, ex.getStatus().value());
    }

    @Test
    void update_throwsWhenNotChoDuyet() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "DA_NHAP_KHO");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));

        PhieuNhapHangRequest req = new PhieuNhapHangRequest(null, null, null, null, "Sua ghi chu", null, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, req));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void delete_successWhenChoDuyet() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "CHO_DUYET");
        ChiTietPhieuNhap line = new ChiTietPhieuNhap();
        line.setId(10L);

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));
        when(chiTietRepository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(1L)).thenReturn(List.of(line));

        service.delete(1L);

        assertEquals("DA_HUY", po.getTrangThai());
        assertNotNull(po.getDeletedAt());
        assertNotNull(line.getDeletedAt());
    }

    @Test
    void delete_throwsWhenAlreadyImported() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "DA_NHAP_KHO");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void approve_incrementsStockForVariantAndToppingAndSetsStatus() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "CHO_DUYET");

        BienTheSanPham bt = new BienTheSanPham();
        bt.setId(10L);
        bt.setSoLuongTon(new BigDecimal("10"));
        bt.setConHang(true);

        Topping tp = new Topping();
        tp.setId(20L);
        tp.setSoLuongTon(BigDecimal.ZERO);
        tp.setConHang(false);

        ChiTietPhieuNhap line1 = new ChiTietPhieuNhap();
        line1.setBienThe(bt);
        line1.setSoLuong(new BigDecimal("5"));

        ChiTietPhieuNhap line2 = new ChiTietPhieuNhap();
        line2.setTopping(tp);
        line2.setSoLuong(new BigDecimal("15"));

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));
        when(chiTietRepository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(1L)).thenReturn(List.of(line1, line2));
        when(bienTheRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(bt));
        when(toppingRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(tp));
        when(repository.save(any(PhieuNhapHang.class))).thenAnswer(call -> call.getArgument(0));
        when(mapper.toResponse(any(PhieuNhapHang.class))).thenReturn(new PhieuNhapHangResponse(1L, "PN-001", new BigDecimal("100000"), null, "DA_NHAP_KHO", Instant.now()));

        PhieuNhapHangResponse res = service.approve(1L);

        assertEquals("DA_NHAP_KHO", po.getTrangThai());
        assertEquals(new BigDecimal("15"), bt.getSoLuongTon());
        assertEquals(new BigDecimal("15"), tp.getSoLuongTon());
        assertTrue(tp.isConHang());
        verify(bienTheRepository).save(bt);
        verify(toppingRepository).save(tp);
        verify(repository).save(po);
    }

    @Test
    void approve_throwsConflictWhenAlreadyApproved() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "DA_NHAP_KHO");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.approve(1L));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    void approve_throwsBadRequestWhenCancelled() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "DA_HUY");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.approve(1L));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void approve_throwsBadRequestWhenNoLines() {
        PhieuNhapHang po = dummyPo(1L, "PN-001", "CHO_DUYET");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(po));
        when(chiTietRepository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.approve(1L));
        assertEquals(400, ex.getStatus().value());
    }

    private PhieuNhapHang dummyPo(Long id, String code, String status) {
        PhieuNhapHang p = new PhieuNhapHang();
        p.setId(id);
        p.setMaPhieuNhap(code);
        p.setTrangThai(status);
        p.setTongTien(BigDecimal.ZERO);
        return p;
    }
}
