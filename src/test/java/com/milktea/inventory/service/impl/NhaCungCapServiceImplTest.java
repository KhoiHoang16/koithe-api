package com.milktea.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.inventory.dto.NhaCungCapRequest;
import com.milktea.inventory.dto.NhaCungCapResponse;
import com.milktea.inventory.entity.NhaCungCap;
import com.milktea.inventory.mapper.NhaCungCapMapper;
import com.milktea.inventory.repository.NhaCungCapRepository;
import com.milktea.inventory.repository.PhieuNhapHangRepository;
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
class NhaCungCapServiceImplTest {

    @Mock NhaCungCapRepository repository;
    @Mock PhieuNhapHangRepository phieuNhapHangRepository;
    @Mock NhaCungCapMapper mapper;

    NhaCungCapServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NhaCungCapServiceImpl(repository, phieuNhapHangRepository, mapper);
    }

    @Test
    void getAll_returnsPagedSuppliers() {
        NhaCungCap ncc = dummySupplier(1L, "Cong ty A", "0901234567");
        NhaCungCapResponse resp = dummyResponse(1L, "Cong ty A", "0901234567");
        when(repository.findAllByDeletedAtIsNull(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ncc)));
        when(mapper.toResponse(ncc)).thenReturn(resp);

        PageResponse<NhaCungCapResponse> page = service.getAll(0, 10);

        assertEquals(1, page.content().size());
        assertEquals("Cong ty A", page.content().get(0).tenNhaCungCap());
    }

    @Test
    void getById_successWhenFound() {
        NhaCungCap ncc = dummySupplier(1L, "Cong ty A", "0901234567");
        NhaCungCapResponse resp = dummyResponse(1L, "Cong ty A", "0901234567");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ncc));
        when(mapper.toResponse(ncc)).thenReturn(resp);

        NhaCungCapResponse result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getById(99L));
        assertEquals(404, ex.getStatus().value());
    }

    @Test
    void create_successWithValidData() {
        NhaCungCapRequest request = new NhaCungCapRequest("Cong ty B", "0912345678", "b@gmail.com", "Dia chi", "MST", "Nguoi dai dien", true, null);
        when(repository.existsBySoDienThoaiAndDeletedAtIsNull("0912345678")).thenReturn(false);
        when(repository.save(any(NhaCungCap.class))).thenAnswer(call -> {
            NhaCungCap s = call.getArgument(0);
            s.setId(2L);
            return s;
        });
        when(mapper.toResponse(any(NhaCungCap.class))).thenReturn(dummyResponse(2L, "Cong ty B", "0912345678"));

        NhaCungCapResponse result = service.create(request);

        assertNotNull(result);
        assertEquals(2L, result.id());
    }

    @Test
    void create_throwsWhenNameIsBlank() {
        NhaCungCapRequest request = new NhaCungCapRequest("   ", "0912345678", null, null, null, null, true, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void create_throwsWhenPhoneIsBlank() {
        NhaCungCapRequest request = new NhaCungCapRequest("Cong ty B", "  ", null, null, null, null, true, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    void create_throwsWhenPhoneAlreadyExists() {
        NhaCungCapRequest request = new NhaCungCapRequest("Cong ty B", "0912345678", null, null, null, null, true, null);
        when(repository.existsBySoDienThoaiAndDeletedAtIsNull("0912345678")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    void update_success() {
        NhaCungCap ncc = dummySupplier(1L, "Cong ty A", "0901234567");
        NhaCungCapRequest request = new NhaCungCapRequest("Cong ty A Moi", "0909999999", null, null, null, null, false, null);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ncc));
        when(repository.existsBySoDienThoaiAndIdNotAndDeletedAtIsNull("0909999999", 1L)).thenReturn(false);
        when(mapper.toResponse(ncc)).thenReturn(dummyResponse(1L, "Cong ty A Moi", "0909999999"));

        NhaCungCapResponse result = service.update(1L, request);

        assertEquals("Cong ty A Moi", ncc.getTenNhaCungCap());
        assertEquals("0909999999", ncc.getSoDienThoai());
        assertFalse(ncc.getDangHopTac());
    }

    @Test
    void update_throwsWhenPhoneConflicts() {
        NhaCungCap ncc = dummySupplier(1L, "Cong ty A", "0901234567");
        NhaCungCapRequest request = new NhaCungCapRequest(null, "0902222222", null, null, null, null, null, null);
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ncc));
        when(repository.existsBySoDienThoaiAndIdNotAndDeletedAtIsNull("0902222222", 1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(1L, request));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    void delete_softDeletesSupplierWhenNoPurchaseOrders() {
        NhaCungCap ncc = dummySupplier(1L, "Cong ty A", "0901234567");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ncc));
        when(phieuNhapHangRepository.existsByNhaCungCapIdAndDeletedAtIsNull(1L)).thenReturn(false);

        service.delete(1L);

        assertNotNull(ncc.getDeletedAt());
    }

    @Test
    void delete_throwsConflictWhenReferencedByPurchaseOrders() {
        NhaCungCap ncc = dummySupplier(1L, "Cong ty A", "0901234567");
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ncc));
        when(phieuNhapHangRepository.existsByNhaCungCapIdAndDeletedAtIsNull(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(1L));
        assertEquals(409, ex.getStatus().value());
    }

    private NhaCungCap dummySupplier(Long id, String ten, String sdt) {
        NhaCungCap s = new NhaCungCap();
        s.setId(id);
        s.setTenNhaCungCap(ten);
        s.setSoDienThoai(sdt);
        s.setDangHopTac(true);
        return s;
    }

    private NhaCungCapResponse dummyResponse(Long id, String ten, String sdt) {
        return new NhaCungCapResponse(id, ten, sdt, null, null, null, null, true);
    }
}
