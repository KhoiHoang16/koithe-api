package com.milktea.promotion.service.impl;

import com.milktea.catalog.entity.LoaiSanPham;
import com.milktea.catalog.entity.SanPham;
import com.milktea.catalog.repository.LoaiSanPhamRepository;
import com.milktea.catalog.repository.SanPhamRepository;
import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.KhuyenMaiSanPhamRequest;
import com.milktea.promotion.dto.KhuyenMaiSanPhamResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.entity.KhuyenMaiSanPham;
import com.milktea.promotion.entity.LoaiGiamGia;
import com.milktea.promotion.mapper.KhuyenMaiSanPhamMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiSanPhamRepository;
import com.milktea.promotion.service.KhuyenMaiSanPhamService;
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
public class KhuyenMaiSanPhamServiceImpl implements KhuyenMaiSanPhamService {

    private final KhuyenMaiSanPhamRepository repository;
    private final KhuyenMaiSanPhamMapper mapper;
    private final ChuongTrinhKhuyenMaiRepository chuongTrinhRepository;
    private final SanPhamRepository sanPhamRepository;
    private final LoaiSanPhamRepository loaiSanPhamRepository;

    public KhuyenMaiSanPhamServiceImpl(
            KhuyenMaiSanPhamRepository repository,
            KhuyenMaiSanPhamMapper mapper,
            ChuongTrinhKhuyenMaiRepository chuongTrinhRepository,
            SanPhamRepository sanPhamRepository,
            LoaiSanPhamRepository loaiSanPhamRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.chuongTrinhRepository = chuongTrinhRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.loaiSanPhamRepository = loaiSanPhamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KhuyenMaiSanPhamResponse> getAll(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tham số phân trang không hợp lệ (page >= 0, size > 0)");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KhuyenMaiSanPhamResponse> responsePage = repository.findByDeletedAtIsNull(pageable)
                .map(mapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public KhuyenMaiSanPhamResponse getById(Long id) {
        KhuyenMaiSanPham entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khuyến mãi sản phẩm với ID: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public KhuyenMaiSanPhamResponse create(KhuyenMaiSanPhamRequest r) {
        validateRequest(r);

        ChuongTrinhKhuyenMai chuongTrinh = chuongTrinhRepository.findByIdAndDeletedAtIsNull(r.maChuongTrinh())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + r.maChuongTrinh()));

        SanPham sanPham = resolveSanPham(r.maSanPham());
        LoaiSanPham danhMuc = resolveDanhMuc(r.maDanhMuc());

        KhuyenMaiSanPham entity = mapper.toEntity(r);
        entity.setChuongTrinh(chuongTrinh);
        entity.setSanPham(sanPham);
        entity.setDanhMuc(danhMuc);

        KhuyenMaiSanPham saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public KhuyenMaiSanPhamResponse update(Long id, KhuyenMaiSanPhamRequest r) {
        validateRequest(r);

        KhuyenMaiSanPham entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khuyến mãi sản phẩm với ID: " + id));

        ChuongTrinhKhuyenMai chuongTrinh = chuongTrinhRepository.findByIdAndDeletedAtIsNull(r.maChuongTrinh())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + r.maChuongTrinh()));

        SanPham sanPham = resolveSanPham(r.maSanPham());
        LoaiSanPham danhMuc = resolveDanhMuc(r.maDanhMuc());

        mapper.updateEntity(entity, r);
        entity.setChuongTrinh(chuongTrinh);
        entity.setSanPham(sanPham);
        entity.setDanhMuc(danhMuc);

        KhuyenMaiSanPham saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        KhuyenMaiSanPham entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khuyến mãi sản phẩm với ID: " + id));
        entity.markDeleted();
        repository.save(entity);
    }

    private void validateRequest(KhuyenMaiSanPhamRequest r) {
        if (r.maChuongTrinh() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã chương trình không được để trống");
        }
        if (r.maSanPham() == null && r.maDanhMuc() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Phải chọn ít nhất một sản phẩm hoặc một danh mục để áp dụng khuyến mãi");
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
    }

    private SanPham resolveSanPham(Long maSanPham) {
        if (maSanPham == null) return null;
        return sanPhamRepository.findById(maSanPham)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm với ID: " + maSanPham));
    }

    private LoaiSanPham resolveDanhMuc(Long maDanhMuc) {
        if (maDanhMuc == null) return null;
        return loaiSanPhamRepository.findById(maDanhMuc)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với ID: " + maDanhMuc));
    }
}
