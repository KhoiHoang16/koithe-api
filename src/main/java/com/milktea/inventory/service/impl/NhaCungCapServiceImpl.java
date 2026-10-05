package com.milktea.inventory.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.inventory.dto.NhaCungCapRequest;
import com.milktea.inventory.dto.NhaCungCapResponse;
import com.milktea.inventory.entity.NhaCungCap;
import com.milktea.inventory.mapper.NhaCungCapMapper;
import com.milktea.inventory.repository.NhaCungCapRepository;
import com.milktea.inventory.repository.PhieuNhapHangRepository;
import com.milktea.inventory.service.NhaCungCapService;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NhaCungCapServiceImpl implements NhaCungCapService {

    private final NhaCungCapRepository repository;
    private final PhieuNhapHangRepository phieuNhapHangRepository;
    private final NhaCungCapMapper mapper;

    public NhaCungCapServiceImpl(
            NhaCungCapRepository repository,
            PhieuNhapHangRepository phieuNhapHangRepository,
            NhaCungCapMapper mapper) {
        this.repository = repository;
        this.phieuNhapHangRepository = phieuNhapHangRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<NhaCungCapResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return PageResponse.from(repository.findAllByDeletedAtIsNull(pageable).map(mapper::toResponse));
    }

    @Override
    public NhaCungCapResponse getById(Long id) {
        return mapper.toResponse(supplier(id));
    }

    @Override
    @Transactional
    public NhaCungCapResponse create(NhaCungCapRequest r) {
        if (r.tenNhaCungCap() == null || r.tenNhaCungCap().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tên nhà cung cấp không được để trống");
        }
        if (r.soDienThoai() == null || r.soDienThoai().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Số điện thoại không được để trống");
        }

        String sdt = r.soDienThoai().trim();
        if (repository.existsBySoDienThoaiAndDeletedAtIsNull(sdt)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Số điện thoại nhà cung cấp đã tồn tại");
        }

        NhaCungCap ncc = new NhaCungCap();
        ncc.setTenNhaCungCap(r.tenNhaCungCap().trim());
        ncc.setSoDienThoai(sdt);
        ncc.setEmail(r.email() != null ? r.email().trim() : null);
        ncc.setDiaChi(r.diaChi() != null ? r.diaChi().trim() : null);
        ncc.setMaSoThue(r.maSoThue() != null ? r.maSoThue().trim() : null);
        ncc.setNguoiDaiDien(r.nguoiDaiDien() != null ? r.nguoiDaiDien().trim() : null);
        ncc.setDangHopTac(r.dangHopTac() != null ? r.dangHopTac() : Boolean.TRUE);
        ncc.setNgayTao(r.ngayTao() != null ? r.ngayTao() : Instant.now());

        return mapper.toResponse(repository.save(ncc));
    }

    @Override
    @Transactional
    public NhaCungCapResponse update(Long id, NhaCungCapRequest r) {
        NhaCungCap ncc = supplier(id);

        if (r.tenNhaCungCap() != null && !r.tenNhaCungCap().isBlank()) {
            ncc.setTenNhaCungCap(r.tenNhaCungCap().trim());
        }
        if (r.soDienThoai() != null && !r.soDienThoai().isBlank()) {
            String sdt = r.soDienThoai().trim();
            if (repository.existsBySoDienThoaiAndIdNotAndDeletedAtIsNull(sdt, id)) {
                throw new BusinessException(HttpStatus.CONFLICT, "Số điện thoại nhà cung cấp đã tồn tại");
            }
            ncc.setSoDienThoai(sdt);
        }
        if (r.email() != null) {
            ncc.setEmail(r.email().trim());
        }
        if (r.diaChi() != null) {
            ncc.setDiaChi(r.diaChi().trim());
        }
        if (r.maSoThue() != null) {
            ncc.setMaSoThue(r.maSoThue().trim());
        }
        if (r.nguoiDaiDien() != null) {
            ncc.setNguoiDaiDien(r.nguoiDaiDien().trim());
        }
        if (r.dangHopTac() != null) {
            ncc.setDangHopTac(r.dangHopTac());
        }

        return mapper.toResponse(ncc);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NhaCungCap ncc = supplier(id);

        if (phieuNhapHangRepository.existsByNhaCungCapIdAndDeletedAtIsNull(id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Không thể xóa nhà cung cấp đã có phiếu nhập hàng");
        }

        ncc.markDeleted();
    }

    private NhaCungCap supplier(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp"));
    }
}
