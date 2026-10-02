package com.milktea.shift.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.shift.dto.*;
import com.milktea.shift.entity.CaLamViec;
import com.milktea.shift.mapper.CaLamViecMapper;
import com.milktea.shift.repository.CaLamViecRepository;
import com.milktea.shift.service.CaLamViecService;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.repository.NguoiDungRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CaLamViecServiceImpl implements CaLamViecService {

    private static final String DANG_MO = "DANG_MO";
    private static final String DA_DONG = "DA_DONG";

    private final CaLamViecRepository repository;
    private final NguoiDungRepository users;
    private final CaLamViecMapper mapper;

    public CaLamViecServiceImpl(CaLamViecRepository repository, NguoiDungRepository users, CaLamViecMapper mapper) {
        this.repository = repository;
        this.users = users;
        this.mapper = mapper;
    }

    @Override
    public List<CaLamViecResponse> findAll() {
        return repository.findAllByDeletedAtIsNullOrderByThoiGianBatDauDesc()
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public CaLamViecResponse findById(Long id) {
        return mapper.toResponse(shift(id));
    }

    @Override
    @Transactional
    public CaLamViecResponse create(CaLamViecRequest r) {
        // Validate thu ngân tồn tại và đang hoạt động
        if (r.maThuNgan() == null) {
            throw error(HttpStatus.BAD_REQUEST, "Mã thu ngân không được để trống");
        }
        NguoiDung thuNgan = users.findById(r.maThuNgan())
                .filter(NguoiDung::isDangHoatDong)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy thu ngân hoặc tài khoản đã bị vô hiệu hóa"));

        // Không cho phép mở đồng thời nhiều ca cho cùng thu ngân
        if (repository.existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(r.maThuNgan(), DANG_MO)) {
            throw error(HttpStatus.CONFLICT, "Thu ngân đang có ca làm việc chưa đóng");
        }

        // Validate tiền đầu ca không âm
        BigDecimal tienDauCa = r.tienDauCa() != null ? r.tienDauCa() : BigDecimal.ZERO;
        if (tienDauCa.compareTo(BigDecimal.ZERO) < 0) {
            throw error(HttpStatus.BAD_REQUEST, "Tiền đầu ca không được âm");
        }

        CaLamViec ca = new CaLamViec();
        ca.setThuNgan(thuNgan);
        ca.setThoiGianBatDau(r.thoiGianBatDau() != null ? r.thoiGianBatDau() : Instant.now());
        ca.setTienDauCa(tienDauCa);
        ca.setTrangThai(DANG_MO);

        return mapper.toResponse(repository.save(ca));
    }

    @Override
    @Transactional
    public CaLamViecResponse update(Long id, CaLamViecRequest r) {
        CaLamViec ca = shift(id);

        // Không cho phép sửa ca đã đóng
        if (DA_DONG.equals(ca.getTrangThai())) {
            throw error(HttpStatus.BAD_REQUEST, "Không thể chỉnh sửa ca đã đóng");
        }

        // Xử lý đóng ca
        if (DA_DONG.equals(r.trangThai())) {
            if (r.tienKetCa() == null) {
                throw error(HttpStatus.BAD_REQUEST, "Tiền kết ca không được để trống khi đóng ca");
            }
            if (r.tienKetCa().compareTo(BigDecimal.ZERO) < 0) {
                throw error(HttpStatus.BAD_REQUEST, "Tiền kết ca không được âm");
            }
            ca.setTienKetCa(r.tienKetCa());
            ca.setThoiGianKetThuc(r.thoiGianKetThuc() != null ? r.thoiGianKetThuc() : Instant.now());
            ca.setTrangThai(DA_DONG);
        } else {
            // Cập nhật thông tin ca đang mở (vd: sửa tiền đầu ca)
            if (r.tienDauCa() != null) {
                if (r.tienDauCa().compareTo(BigDecimal.ZERO) < 0) {
                    throw error(HttpStatus.BAD_REQUEST, "Tiền đầu ca không được âm");
                }
                ca.setTienDauCa(r.tienDauCa());
            }
        }

        return mapper.toResponse(ca);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CaLamViec ca = shift(id);
        ca.markDeleted();
    }

    // ─── private helpers ────────────────────────────────────────────────

    private CaLamViec shift(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Không tìm thấy ca làm việc"));
    }

    private BusinessException error(HttpStatus status, String message) {
        return new BusinessException(status, message);
    }
}
