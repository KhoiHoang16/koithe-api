package com.milktea.promotion.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.KhuyenMaiHoaDonRequest;
import com.milktea.promotion.dto.KhuyenMaiHoaDonResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.entity.KhuyenMaiHoaDon;
import com.milktea.promotion.entity.LoaiGiamGia;
import com.milktea.promotion.mapper.KhuyenMaiHoaDonMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiHoaDonRepository;
import com.milktea.promotion.service.KhuyenMaiHoaDonService;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KhuyenMaiHoaDonServiceImpl implements KhuyenMaiHoaDonService {

    private final KhuyenMaiHoaDonRepository repository;
    private final KhuyenMaiHoaDonMapper mapper;
    private final ChuongTrinhKhuyenMaiRepository chuongTrinhRepository;

    public KhuyenMaiHoaDonServiceImpl(
            KhuyenMaiHoaDonRepository repository,
            KhuyenMaiHoaDonMapper mapper,
            ChuongTrinhKhuyenMaiRepository chuongTrinhRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.chuongTrinhRepository = chuongTrinhRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KhuyenMaiHoaDonResponse> getAll(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tham số phân trang không hợp lệ (page >= 0, size > 0)");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KhuyenMaiHoaDonResponse> responsePage = repository.findByDeletedAtIsNull(pageable)
                .map(mapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public KhuyenMaiHoaDonResponse getById(Long id) {
        KhuyenMaiHoaDon entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khuyến mãi hóa đơn với ID: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public KhuyenMaiHoaDonResponse create(KhuyenMaiHoaDonRequest r) {
        validateRequest(r);

        ChuongTrinhKhuyenMai chuongTrinh = chuongTrinhRepository.findByIdAndDeletedAtIsNull(r.maChuongTrinh())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + r.maChuongTrinh()));

        KhuyenMaiHoaDon entity = mapper.toEntity(r);
        entity.setChuongTrinh(chuongTrinh);

        KhuyenMaiHoaDon saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public KhuyenMaiHoaDonResponse update(Long id, KhuyenMaiHoaDonRequest r) {
        validateRequest(r);

        KhuyenMaiHoaDon entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khuyến mãi hóa đơn với ID: " + id));

        ChuongTrinhKhuyenMai chuongTrinh = chuongTrinhRepository.findByIdAndDeletedAtIsNull(r.maChuongTrinh())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + r.maChuongTrinh()));

        mapper.updateEntity(entity, r);
        entity.setChuongTrinh(chuongTrinh);

        KhuyenMaiHoaDon saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        KhuyenMaiHoaDon entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khuyến mãi hóa đơn với ID: " + id));
        entity.markDeleted();
        repository.save(entity);
    }

    private void validateRequest(KhuyenMaiHoaDonRequest r) {
        if (r.maChuongTrinh() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã chương trình không được để trống");
        }
        if (r.donHangToiThieu() == null || r.donHangToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng tối thiểu không được âm");
        }
        if (r.loaiGiamGia() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Loại giảm giá không được để trống");
        }
        if (r.giaTriGiam() == null || r.giaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Giá trị giảm phải lớn hơn 0");
        }
        if (r.loaiGiamGia() == LoaiGiamGia.PHAN_TRAM && r.giaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Khuyến mãi theo phần trăm không được vượt quá 100%");
        }
        if (r.mucGiamToiDa() != null && r.mucGiamToiDa().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mức giảm tối đa không được âm");
        }
    }
}
