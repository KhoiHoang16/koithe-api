package com.milktea.promotion.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiRequest;
import com.milktea.promotion.dto.ChuongTrinhKhuyenMaiResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.mapper.ChuongTrinhKhuyenMaiMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiHoaDonRepository;
import com.milktea.promotion.repository.KhuyenMaiSanPhamRepository;
import com.milktea.promotion.repository.KhuyenMaiVoucherRepository;
import com.milktea.promotion.service.ChuongTrinhKhuyenMaiService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChuongTrinhKhuyenMaiServiceImpl implements ChuongTrinhKhuyenMaiService {

    private final ChuongTrinhKhuyenMaiRepository repository;
    private final ChuongTrinhKhuyenMaiMapper mapper;
    private final KhuyenMaiSanPhamRepository kmSanPhamRepository;
    private final KhuyenMaiHoaDonRepository kmHoaDonRepository;
    private final KhuyenMaiVoucherRepository kmVoucherRepository;

    public ChuongTrinhKhuyenMaiServiceImpl(
            ChuongTrinhKhuyenMaiRepository repository,
            ChuongTrinhKhuyenMaiMapper mapper,
            KhuyenMaiSanPhamRepository kmSanPhamRepository,
            KhuyenMaiHoaDonRepository kmHoaDonRepository,
            KhuyenMaiVoucherRepository kmVoucherRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.kmSanPhamRepository = kmSanPhamRepository;
        this.kmHoaDonRepository = kmHoaDonRepository;
        this.kmVoucherRepository = kmVoucherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChuongTrinhKhuyenMaiResponse> getAll(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tham số phân trang không hợp lệ (page >= 0, size > 0)");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChuongTrinhKhuyenMaiResponse> responsePage = repository.findByDeletedAtIsNull(pageable)
                .map(mapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ChuongTrinhKhuyenMaiResponse getById(Long id) {
        ChuongTrinhKhuyenMai entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public ChuongTrinhKhuyenMaiResponse create(ChuongTrinhKhuyenMaiRequest r) {
        validateRequest(r);
        ChuongTrinhKhuyenMai entity = mapper.toEntity(r);
        ChuongTrinhKhuyenMai saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public ChuongTrinhKhuyenMaiResponse update(Long id, ChuongTrinhKhuyenMaiRequest r) {
        validateRequest(r);
        ChuongTrinhKhuyenMai entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + id));

        mapper.updateEntity(entity, r);
        ChuongTrinhKhuyenMai saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        ChuongTrinhKhuyenMai entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + id));

        boolean hasChildPromotion = kmSanPhamRepository.existsByChuongTrinhIdAndDeletedAtIsNull(id)
                || kmHoaDonRepository.existsByChuongTrinhIdAndDeletedAtIsNull(id)
                || kmVoucherRepository.existsByChuongTrinhIdAndDeletedAtIsNull(id);

        if (hasChildPromotion) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Không thể xóa chương trình khuyến mãi đang có cấu hình khuyến mãi sản phẩm, hóa đơn hoặc voucher liên kết");
        }

        entity.markDeleted();
        repository.save(entity);
    }

    private void validateRequest(ChuongTrinhKhuyenMaiRequest r) {
        if (r.tenChuongTrinh() == null || r.tenChuongTrinh().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tên chương trình không được để trống");
        }
        if (r.ngayBatDau() == null || r.ngayKetThuc() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (r.ngayBatDau().isAfter(r.ngayKetThuc())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        if (r.dangHoatDong() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Trạng thái hoạt động không được để trống");
        }
    }
}
