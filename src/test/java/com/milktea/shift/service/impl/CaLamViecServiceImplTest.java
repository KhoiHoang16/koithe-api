package com.milktea.shift.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.shift.dto.CaLamViecRequest;
import com.milktea.shift.dto.CaLamViecResponse;
import com.milktea.shift.entity.CaLamViec;
import com.milktea.shift.mapper.CaLamViecMapper;
import com.milktea.shift.repository.CaLamViecRepository;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaLamViecServiceImplTest {

    @Mock CaLamViecRepository repository;
    @Mock NguoiDungRepository users;
    @Mock CaLamViecMapper mapper;

    CaLamViecServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CaLamViecServiceImpl(repository, users, mapper);
    }

    // ─── findAll ────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllNonDeletedShifts() {
        CaLamViec ca1 = openShift(1L);
        CaLamViec ca2 = openShift(2L);
        CaLamViecResponse resp1 = dummyResponse(1L, "DANG_MO");
        CaLamViecResponse resp2 = dummyResponse(2L, "DANG_MO");

        when(repository.findAllByDeletedAtIsNullOrderByThoiGianBatDauDesc()).thenReturn(List.of(ca1, ca2));
        when(mapper.toResponse(ca1)).thenReturn(resp1);
        when(mapper.toResponse(ca2)).thenReturn(resp2);

        List<CaLamViecResponse> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository).findAllByDeletedAtIsNullOrderByThoiGianBatDauDesc();
    }

    // ─── findById ───────────────────────────────────────────────────────

    @Test
    void findById_returnsShiftWhenExists() {
        CaLamViec ca = openShift(1L);
        CaLamViecResponse resp = dummyResponse(1L, "DANG_MO");

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));
        when(mapper.toResponse(ca)).thenReturn(resp);

        CaLamViecResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.findById(99L));
        assertEquals(404, ex.getStatus().value());
    }

    // ─── create (mở ca) ────────────────────────────────────────────────

    @Test
    void create_successWithValidCashier() {
        NguoiDung cashier = activeCashier(3L);
        CaLamViecRequest request = new CaLamViecRequest(3L, null, null, new BigDecimal("200000"), null, null);

        when(users.findById(3L)).thenReturn(Optional.of(cashier));
        when(repository.existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(3L, "DANG_MO")).thenReturn(false);
        when(repository.save(any(CaLamViec.class))).thenAnswer(call -> {
            CaLamViec ca = call.getArgument(0);
            ca.setId(1L);
            return ca;
        });
        when(mapper.toResponse(any(CaLamViec.class))).thenReturn(dummyResponse(1L, "DANG_MO"));

        CaLamViecResponse result = service.create(request);

        assertNotNull(result);
        verify(repository).save(any(CaLamViec.class));
    }

    @Test
    void create_throwsWhenCashierNotFound() {
        CaLamViecRequest request = new CaLamViecRequest(99L, null, null, BigDecimal.ZERO, null, null);

        when(users.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(404, ex.getStatus().value());
        verifyNoInteractions(repository);
    }

    @Test
    void create_throwsWhenCashierInactive() {
        NguoiDung cashier = new NguoiDung();
        cashier.setId(3L);
        cashier.setDangHoatDong(false);
        CaLamViecRequest request = new CaLamViecRequest(3L, null, null, BigDecimal.ZERO, null, null);

        when(users.findById(3L)).thenReturn(Optional.of(cashier));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(404, ex.getStatus().value());
        verify(repository, never()).save(any());
    }

    @Test
    void create_throwsWhenCashierAlreadyHasOpenShift() {
        NguoiDung cashier = activeCashier(3L);
        CaLamViecRequest request = new CaLamViecRequest(3L, null, null, new BigDecimal("100000"), null, null);

        when(users.findById(3L)).thenReturn(Optional.of(cashier));
        when(repository.existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(3L, "DANG_MO")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(409, ex.getStatus().value());
        verify(repository, never()).save(any());
    }

    @Test
    void create_throwsWhenInitialCashIsNegative() {
        NguoiDung cashier = activeCashier(3L);
        CaLamViecRequest request = new CaLamViecRequest(3L, null, null, new BigDecimal("-1"), null, null);

        when(users.findById(3L)).thenReturn(Optional.of(cashier));
        when(repository.existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(3L, "DANG_MO")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(400, ex.getStatus().value());
        verify(repository, never()).save(any());
    }

    @Test
    void create_throwsWhenMaThuNganIsNull() {
        CaLamViecRequest request = new CaLamViecRequest(null, null, null, BigDecimal.ZERO, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void create_defaultsInitialCashToZeroWhenNull() {
        NguoiDung cashier = activeCashier(3L);
        CaLamViecRequest request = new CaLamViecRequest(3L, null, null, null, null, null);

        when(users.findById(3L)).thenReturn(Optional.of(cashier));
        when(repository.existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(3L, "DANG_MO")).thenReturn(false);
        when(repository.save(any(CaLamViec.class))).thenAnswer(call -> {
            CaLamViec ca = call.getArgument(0);
            ca.setId(1L);
            return ca;
        });
        when(mapper.toResponse(any(CaLamViec.class))).thenReturn(dummyResponse(1L, "DANG_MO"));

        service.create(request);

        verify(repository).save(argThat(ca -> ca.getTienDauCa().compareTo(BigDecimal.ZERO) == 0));
    }

    // ─── update (đóng ca / sửa ca) ─────────────────────────────────────

    @Test
    void update_closeShiftSuccessfully() {
        CaLamViec ca = openShift(1L);
        CaLamViecRequest request = new CaLamViecRequest(null, null, null, null, new BigDecimal("550000"), "DA_DONG");

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));
        when(mapper.toResponse(ca)).thenReturn(dummyResponse(1L, "DA_DONG"));

        CaLamViecResponse result = service.update(1L, request);

        assertEquals("DA_DONG", ca.getTrangThai());
        assertEquals(new BigDecimal("550000"), ca.getTienKetCa());
        assertNotNull(ca.getThoiGianKetThuc());
    }

    @Test
    void update_throwsWhenShiftAlreadyClosed() {
        CaLamViec ca = closedShift(1L);
        CaLamViecRequest request = new CaLamViecRequest(null, null, null, null, new BigDecimal("550000"), "DA_DONG");

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void update_throwsWhenClosingCashIsNull() {
        CaLamViec ca = openShift(1L);
        CaLamViecRequest request = new CaLamViecRequest(null, null, null, null, null, "DA_DONG");

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void update_throwsWhenClosingCashIsNegative() {
        CaLamViec ca = openShift(1L);
        CaLamViecRequest request = new CaLamViecRequest(null, null, null, null, new BigDecimal("-100"), "DA_DONG");

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void update_canUpdateInitialCashOnOpenShift() {
        CaLamViec ca = openShift(1L);
        CaLamViecRequest request = new CaLamViecRequest(null, null, null, new BigDecimal("300000"), null, "DANG_MO");

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));
        when(mapper.toResponse(ca)).thenReturn(dummyResponse(1L, "DANG_MO"));

        service.update(1L, request);

        assertEquals(new BigDecimal("300000"), ca.getTienDauCa());
        assertEquals("DANG_MO", ca.getTrangThai());
    }

    @Test
    void update_rejectsNegativeInitialCashUpdate() {
        CaLamViec ca = openShift(1L);
        CaLamViecRequest request = new CaLamViecRequest(null, null, null, new BigDecimal("-1"), null, "DANG_MO");

        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(400, ex.getStatus().value());
    }

    // ─── delete ─────────────────────────────────────────────────────────

    @Test
    void delete_softDeletesShift() {
        CaLamViec ca = openShift(1L);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ca));

        service.delete(1L);

        assertNotNull(ca.getDeletedAt());
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(99L));
    }

    @Test
    void findAll_withStatus_filtersByStatus() {
        CaLamViec ca1 = openShift(1L);
        CaLamViecResponse resp1 = dummyResponse(1L, "DANG_MO");
        when(repository.findAllByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc("DANG_MO"))
                .thenReturn(List.of(ca1));
        when(mapper.toResponse(ca1)).thenReturn(resp1);

        List<CaLamViecResponse> result = service.findAll("DANG_MO");

        assertEquals(1, result.size());
        verify(repository).findAllByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc("DANG_MO");
    }

    @Test
    void findCurrentActive_returnsActiveShiftWhenPresent() {
        CaLamViec ca1 = openShift(1L);
        CaLamViecResponse resp1 = dummyResponse(1L, "DANG_MO");
        when(repository.findFirstByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc("DANG_MO"))
                .thenReturn(Optional.of(ca1));
        when(mapper.toResponse(ca1)).thenReturn(resp1);

        CaLamViecResponse result = service.findCurrentActive();

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void findCurrentActive_returnsNullWhenNoActiveShift() {
        when(repository.findFirstByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc("DANG_MO"))
                .thenReturn(Optional.empty());

        CaLamViecResponse result = service.findCurrentActive();

        assertNull(result);
    }

    // ─── helpers ────────────────────────────────────────────────────────

    private NguoiDung activeCashier(Long id) {
        NguoiDung cashier = new NguoiDung();
        cashier.setId(id);
        cashier.setDangHoatDong(true);
        cashier.setHoVaTen("Thu Ngân " + id);
        return cashier;
    }

    private CaLamViec openShift(Long id) {
        CaLamViec ca = new CaLamViec();
        ca.setId(id);
        ca.setThuNgan(activeCashier(3L));
        ca.setThoiGianBatDau(Instant.now());
        ca.setTienDauCa(new BigDecimal("200000"));
        ca.setTrangThai("DANG_MO");
        return ca;
    }

    private CaLamViec closedShift(Long id) {
        CaLamViec ca = openShift(id);
        ca.setTrangThai("DA_DONG");
        ca.setTienKetCa(new BigDecimal("550000"));
        ca.setThoiGianKetThuc(Instant.now());
        return ca;
    }

    private CaLamViecResponse dummyResponse(Long id, String trangThai) {
        return new CaLamViecResponse(id, Instant.now(), null,
                new BigDecimal("200000"), null, trangThai);
    }
}
