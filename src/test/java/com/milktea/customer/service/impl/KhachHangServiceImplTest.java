package com.milktea.customer.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.milktea.common.exception.BusinessException;
import com.milktea.customer.dto.*;
import com.milktea.customer.entity.HangThanhVien;
import com.milktea.customer.entity.KhachHang;
import com.milktea.customer.mapper.KhachHangMapper;
import com.milktea.customer.repository.KhachHangRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class KhachHangServiceImplTest {

    @Mock KhachHangRepository repo;
    @Mock KhachHangMapper mapper;
    @Mock PasswordEncoder encoder;
    KhachHangServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KhachHangServiceImpl(repo, mapper, encoder);
    }

    @Test
    void createCustomer_Success() {
        KhachHangCreateRequest request = new KhachHangCreateRequest("0901234567", "Nguyen Van A", "a@gmail.com", "123456");
        KhachHang entity = new KhachHang();
        KhachHangResponse response = new KhachHangResponse(1L, "0901234567", "Nguyen Van A", "a@gmail.com",
                0, HangThanhVien.DONG, false, null, null, null);

        when(repo.existsBySoDienThoaiAndDeletedAtIsNull("0901234567")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(encoder.encode("123456")).thenReturn("$2a$10$hashed");
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        KhachHangResponse result = service.create(request);

        assertEquals("0901234567", result.soDienThoai());
        assertEquals(HangThanhVien.DONG, result.hangThanhVien());
        assertEquals(0, result.diemTichLuy());
        verify(encoder).encode("123456");
        verify(repo).save(entity);
    }

    @Test
    void createCustomer_ThrowsConflict_WhenPhoneExists() {
        KhachHangCreateRequest request = new KhachHangCreateRequest("0901234567", "Nguyen Van A", "a@gmail.com", "123456");

        when(repo.existsBySoDienThoaiAndDeletedAtIsNull("0901234567")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    void findById_Success() {
        KhachHang entity = new KhachHang();
        entity.setSoDienThoai("0901234567");
        KhachHangResponse response = new KhachHangResponse(1L, "0901234567", "Test", null,
                0, HangThanhVien.DONG, false, null, null, null);

        when(repo.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        KhachHangResponse result = service.findById(1L);
        assertEquals("0901234567", result.soDienThoai());
    }

    @Test
    void findById_ThrowsNotFound_WhenIdInvalid() {
        when(repo.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.findById(999L));
        assertEquals(404, ex.getStatus().value());
    }

    @Test
    void findByPhone_Success() {
        KhachHang entity = new KhachHang();
        entity.setSoDienThoai("0901234567");
        KhachHangResponse response = new KhachHangResponse(1L, "0901234567", "Test", null,
                0, HangThanhVien.DONG, false, null, null, null);

        when(repo.findBySoDienThoaiAndDeletedAtIsNull("0901234567")).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        KhachHangResponse result = service.findByPhone("0901234567");
        assertEquals("0901234567", result.soDienThoai());
    }

    @Test
    void deleteCustomer_SoftDeletes() {
        KhachHang entity = new KhachHang();
        when(repo.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(entity));

        service.delete(1L);

        assertNotNull(entity.getDeletedAt());
    }
}
