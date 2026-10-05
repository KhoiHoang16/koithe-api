package com.milktea.table.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.table.dto.BanRequest;
import com.milktea.table.dto.BanResponse;
import com.milktea.table.entity.Ban;
import com.milktea.table.mapper.BanMapper;
import com.milktea.table.repository.BanRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BanServiceImplTest {

    @Mock BanRepository repository;
    @Mock BanMapper mapper;

    BanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BanServiceImpl(repository, mapper);
    }

    // ─── findAll ────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllNonDeletedTables() {
        Ban b1 = table(1L, "Ban 01", "TRONG");
        Ban b2 = table(2L, "Ban 02", "DANG_CO_KHACH");
        when(repository.findAllByDeletedAtIsNullOrderBySoBanAsc()).thenReturn(List.of(b1, b2));
        when(mapper.toResponse(b1)).thenReturn(response(1L, "Ban 01", "token1", "TRONG"));
        when(mapper.toResponse(b2)).thenReturn(response(2L, "Ban 02", "token2", "DANG_CO_KHACH"));

        List<BanResponse> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository).findAllByDeletedAtIsNullOrderBySoBanAsc();
    }

    // ─── findById ───────────────────────────────────────────────────────

    @Test
    void findById_returnsTableWhenExists() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 01", "token1", "TRONG"));

        BanResponse result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.findById(99L));
        assertEquals(404, ex.getStatus().value());
    }

    // ─── create ─────────────────────────────────────────────────────────

    @Test
    void create_successWithValidTableNumber() {
        BanRequest request = new BanRequest("Ban 11", null, null);
        when(repository.existsBySoBanAndDeletedAtIsNull("Ban 11")).thenReturn(false);
        when(repository.save(any(Ban.class))).thenAnswer(call -> {
            Ban b = call.getArgument(0);
            b.setId(11L);
            return b;
        });
        when(mapper.toResponse(any(Ban.class))).thenReturn(response(11L, "Ban 11", "uuid", "TRONG"));

        BanResponse result = service.create(request);

        assertNotNull(result);
        verify(repository).save(argThat(b -> {
            assertEquals("Ban 11", b.getSoBan());
            assertNotNull(b.getMaQrToken());
            assertEquals("TRONG", b.getTrangThai());
            return true;
        }));
    }

    @Test
    void create_generatesUniqueQrToken() {
        BanRequest request = new BanRequest("Ban 11", null, null);
        when(repository.existsBySoBanAndDeletedAtIsNull("Ban 11")).thenReturn(false);
        when(repository.save(any(Ban.class))).thenAnswer(call -> call.getArgument(0));
        when(mapper.toResponse(any(Ban.class))).thenReturn(response(11L, "Ban 11", "uuid", "TRONG"));

        service.create(request);

        verify(repository).save(argThat(b -> {
            assertNotNull(b.getMaQrToken());
            assertFalse(b.getMaQrToken().isBlank());
            return true;
        }));
    }

    @Test
    void create_throwsWhenTableNumberIsNull() {
        BanRequest request = new BanRequest(null, null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void create_throwsWhenTableNumberIsBlank() {
        BanRequest request = new BanRequest("   ", null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void create_throwsWhenTableNumberAlreadyExists() {
        BanRequest request = new BanRequest("Ban 01", null, null);
        when(repository.existsBySoBanAndDeletedAtIsNull("Ban 01")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(409, ex.getStatus().value());
        verify(repository, never()).save(any());
    }

    @Test
    void create_alwaysSetsStatusToTrong() {
        // Ngay cả khi client gửi trangThai khác, create luôn đặt TRONG
        BanRequest request = new BanRequest("Ban 11", null, "DANG_CO_KHACH");
        when(repository.existsBySoBanAndDeletedAtIsNull("Ban 11")).thenReturn(false);
        when(repository.save(any(Ban.class))).thenAnswer(call -> call.getArgument(0));
        when(mapper.toResponse(any(Ban.class))).thenReturn(response(11L, "Ban 11", "uuid", "TRONG"));

        service.create(request);

        verify(repository).save(argThat(b -> "TRONG".equals(b.getTrangThai())));
    }

    // ─── update ─────────────────────────────────────────────────────────

    @Test
    void update_canChangeTableNumber() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        BanRequest request = new BanRequest("Ban 99", null, null);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(repository.existsBySoBanAndIdNotAndDeletedAtIsNull("Ban 99", 1L)).thenReturn(false);
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 99", "token1", "TRONG"));

        service.update(1L, request);

        assertEquals("Ban 99", ban.getSoBan());
    }

    @Test
    void update_throwsWhenNewTableNumberConflicts() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        BanRequest request = new BanRequest("Ban 02", null, null);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(repository.existsBySoBanAndIdNotAndDeletedAtIsNull("Ban 02", 1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    void update_canChangeStatusFromTrongToDangCoKhach() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        BanRequest request = new BanRequest(null, null, "DANG_CO_KHACH");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 01", "token1", "DANG_CO_KHACH"));

        service.update(1L, request);

        assertEquals("DANG_CO_KHACH", ban.getTrangThai());
    }

    @Test
    void update_canChangeStatusFromTrongToDaDatTruoc() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        BanRequest request = new BanRequest(null, null, "DA_DAT_TRUOC");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 01", "token1", "DA_DAT_TRUOC"));

        service.update(1L, request);

        assertEquals("DA_DAT_TRUOC", ban.getTrangThai());
    }

    @Test
    void update_canChangeStatusFromDangCoKhachToTrong() {
        Ban ban = table(1L, "Ban 01", "DANG_CO_KHACH");
        BanRequest request = new BanRequest(null, null, "TRONG");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 01", "token1", "TRONG"));

        service.update(1L, request);

        assertEquals("TRONG", ban.getTrangThai());
    }

    @Test
    void update_rejectsInvalidStatusFromDangCoKhach() {
        Ban ban = table(1L, "Ban 01", "DANG_CO_KHACH");
        BanRequest request = new BanRequest(null, null, "DA_DAT_TRUOC");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void update_canChangeStatusFromDaDatTruocToDangCoKhach() {
        Ban ban = table(1L, "Ban 01", "DA_DAT_TRUOC");
        BanRequest request = new BanRequest(null, null, "DANG_CO_KHACH");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 01", "token1", "DANG_CO_KHACH"));

        service.update(1L, request);

        assertEquals("DANG_CO_KHACH", ban.getTrangThai());
    }

    @Test
    void update_canChangeStatusFromDaDatTruocToTrong() {
        Ban ban = table(1L, "Ban 01", "DA_DAT_TRUOC");
        BanRequest request = new BanRequest(null, null, "TRONG");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 01", "token1", "TRONG"));

        service.update(1L, request);

        assertEquals("TRONG", ban.getTrangThai());
    }

    @Test
    void update_rejectsInvalidStatusValue() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        BanRequest request = new BanRequest(null, null, "INVALID");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void update_rotatesQrTokenWhenRequested() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        String oldToken = ban.getMaQrToken();
        BanRequest request = new BanRequest(null, "rotate", null);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));
        when(mapper.toResponse(ban)).thenReturn(response(1L, "Ban 01", "new-token", "TRONG"));

        service.update(1L, request);

        assertNotEquals(oldToken, ban.getMaQrToken());
    }

    // ─── delete ─────────────────────────────────────────────────────────

    @Test
    void delete_softDeletesEmptyTable() {
        Ban ban = table(1L, "Ban 01", "TRONG");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));

        service.delete(1L);

        assertNotNull(ban.getDeletedAt());
    }

    @Test
    void delete_throwsWhenTableIsOccupied() {
        Ban ban = table(1L, "Ban 01", "DANG_CO_KHACH");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    void delete_throwsWhenTableIsReserved() {
        Ban ban = table(1L, "Ban 01", "DA_DAT_TRUOC");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ban));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(99L));
        assertEquals(404, ex.getStatus().value());
    }

    // ─── helpers ────────────────────────────────────────────────────────

    private Ban table(Long id, String soBan, String trangThai) {
        Ban ban = new Ban();
        ban.setId(id);
        ban.setSoBan(soBan);
        ban.setMaQrToken("test-token-" + id);
        ban.setTrangThai(trangThai);
        return ban;
    }

    private BanResponse response(Long id, String soBan, String maQrToken, String trangThai) {
        return new BanResponse(id, soBan, maQrToken, trangThai);
    }
}
