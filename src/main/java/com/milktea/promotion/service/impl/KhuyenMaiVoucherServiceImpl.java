package com.milktea.promotion.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.promotion.dto.KhuyenMaiVoucherRequest;
import com.milktea.promotion.dto.KhuyenMaiVoucherResponse;
import com.milktea.promotion.dto.ValidateVoucherRequest;
import com.milktea.promotion.dto.ValidateVoucherResponse;
import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import com.milktea.promotion.entity.KhuyenMaiVoucher;
import com.milktea.promotion.entity.LoaiGiamGia;
import com.milktea.promotion.mapper.KhuyenMaiVoucherMapper;
import com.milktea.promotion.repository.ChuongTrinhKhuyenMaiRepository;
import com.milktea.promotion.repository.KhuyenMaiVoucherRepository;
import com.milktea.promotion.service.KhuyenMaiVoucherService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KhuyenMaiVoucherServiceImpl implements KhuyenMaiVoucherService {

    private final KhuyenMaiVoucherRepository repository;
    private final KhuyenMaiVoucherMapper mapper;
    private final ChuongTrinhKhuyenMaiRepository chuongTrinhRepository;

    public KhuyenMaiVoucherServiceImpl(
            KhuyenMaiVoucherRepository repository,
            KhuyenMaiVoucherMapper mapper,
            ChuongTrinhKhuyenMaiRepository chuongTrinhRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.chuongTrinhRepository = chuongTrinhRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KhuyenMaiVoucherResponse> getAll(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tham số phân trang không hợp lệ (page >= 0, size > 0)");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KhuyenMaiVoucherResponse> responsePage = repository.findByDeletedAtIsNull(pageable)
                .map(mapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public KhuyenMaiVoucherResponse getById(Long id) {
        KhuyenMaiVoucher entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher với ID: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public KhuyenMaiVoucherResponse create(KhuyenMaiVoucherRequest r) {
        validateRequest(r);

        String normalizedCode = r.maCode().trim().toUpperCase();
        if (repository.existsByMaCodeIgnoreCaseAndDeletedAtIsNull(normalizedCode)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Mã voucher đã tồn tại: " + normalizedCode);
        }

        ChuongTrinhKhuyenMai chuongTrinh = chuongTrinhRepository.findByIdAndDeletedAtIsNull(r.maChuongTrinh())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + r.maChuongTrinh()));

        KhuyenMaiVoucher entity = mapper.toEntity(r);
        entity.setMaCode(normalizedCode);
        entity.setChuongTrinh(chuongTrinh);
        entity.setSoLuotDaDung(r.soLuotDaDung() != null ? r.soLuotDaDung() : 0);

        KhuyenMaiVoucher saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public KhuyenMaiVoucherResponse update(Long id, KhuyenMaiVoucherRequest r) {
        validateRequest(r);

        KhuyenMaiVoucher entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher với ID: " + id));

        String normalizedCode = r.maCode().trim().toUpperCase();
        if (!entity.getMaCode().equalsIgnoreCase(normalizedCode)
                && repository.existsByMaCodeIgnoreCaseAndDeletedAtIsNull(normalizedCode)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Mã voucher đã tồn tại: " + normalizedCode);
        }

        ChuongTrinhKhuyenMai chuongTrinh = chuongTrinhRepository.findByIdAndDeletedAtIsNull(r.maChuongTrinh())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi với ID: " + r.maChuongTrinh()));

        mapper.updateEntity(entity, r);
        entity.setMaCode(normalizedCode);
        entity.setChuongTrinh(chuongTrinh);

        KhuyenMaiVoucher saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        KhuyenMaiVoucher entity = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher với ID: " + id));
        entity.markDeleted();
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidateVoucherResponse validateVoucher(ValidateVoucherRequest r) {
        if (r.maCode() == null || r.maCode().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã voucher không được để trống");
        }
        if (r.tongTienDonHang() == null || r.tongTienDonHang().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Tổng tiền đơn hàng không hợp lệ");
        }

        String normalizedCode = r.maCode().trim().toUpperCase();
        KhuyenMaiVoucher voucher = repository.findByMaCodeIgnoreCaseAndDeletedAtIsNull(normalizedCode).orElse(null);

        if (voucher == null) {
            return new ValidateVoucherResponse(normalizedCode, false, BigDecimal.ZERO, r.tongTienDonHang(), "Mã voucher không tồn tại");
        }

        ChuongTrinhKhuyenMai chuongTrinh = voucher.getChuongTrinh();
        if (chuongTrinh == null || Boolean.FALSE.equals(chuongTrinh.getDangHoatDong()) || chuongTrinh.getDeletedAt() != null) {
            return new ValidateVoucherResponse(normalizedCode, false, BigDecimal.ZERO, r.tongTienDonHang(), "Chương trình khuyến mãi của voucher đang không hoạt động");
        }

        Instant now = Instant.now();
        if (now.isBefore(chuongTrinh.getNgayBatDau())) {
            return new ValidateVoucherResponse(normalizedCode, false, BigDecimal.ZERO, r.tongTienDonHang(), "Chương trình khuyến mãi chưa bắt đầu");
        }
        if (now.isAfter(chuongTrinh.getNgayKetThuc())) {
            return new ValidateVoucherResponse(normalizedCode, false, BigDecimal.ZERO, r.tongTienDonHang(), "Chương trình khuyến mãi đã kết thúc");
        }

        if (voucher.getGioiHanSuDung() != null && voucher.getSoLuotDaDung() >= voucher.getGioiHanSuDung()) {
            return new ValidateVoucherResponse(normalizedCode, false, BigDecimal.ZERO, r.tongTienDonHang(), "Voucher đã hết lượt sử dụng");
        }

        BigDecimal donHangToiThieu = voucher.getDonHangToiThieu() != null ? voucher.getDonHangToiThieu() : BigDecimal.ZERO;
        if (r.tongTienDonHang().compareTo(donHangToiThieu) < 0) {
            return new ValidateVoucherResponse(normalizedCode, false, BigDecimal.ZERO, r.tongTienDonHang(),
                    String.format("Đơn hàng chưa đạt giá trị tối thiểu (%,.0f đ)", donHangToiThieu));
        }

        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (voucher.getLoaiGiamGia() == LoaiGiamGia.TIEN_CO_DINH) {
            soTienGiam = voucher.getGiaTriGiam();
        } else if (voucher.getLoaiGiamGia() == LoaiGiamGia.PHAN_TRAM) {
            soTienGiam = r.tongTienDonHang()
                    .multiply(voucher.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getMucGiamToiDa() != null && voucher.getMucGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                soTienGiam = soTienGiam.min(voucher.getMucGiamToiDa());
            }
        }

        soTienGiam = soTienGiam.min(r.tongTienDonHang()).max(BigDecimal.ZERO);
        BigDecimal tongTienSauGiam = r.tongTienDonHang().subtract(soTienGiam).max(BigDecimal.ZERO);

        return new ValidateVoucherResponse(normalizedCode, true, soTienGiam, tongTienSauGiam, "Áp dụng mã voucher thành công");
    }

    @Override
    public ValidateVoucherResponse applyVoucher(ValidateVoucherRequest r) {
        ValidateVoucherResponse validation = validateVoucher(r);
        if (!validation.hopLe()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, validation.thongDiep());
        }

        String normalizedCode = r.maCode().trim().toUpperCase();
        KhuyenMaiVoucher voucher = repository.findByMaCodeIgnoreCaseAndDeletedAtIsNull(normalizedCode)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher: " + normalizedCode));

        int updated = repository.tangSoLuotDaDungAtomic(voucher.getId());
        if (updated == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Voucher đã hết lượt sử dụng");
        }

        return validation;
    }

    @Override
    public void releaseVoucher(String maCode) {
        if (maCode == null || maCode.trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã voucher không được để trống");
        }
        String normalizedCode = maCode.trim().toUpperCase();
        KhuyenMaiVoucher voucher = repository.findByMaCodeIgnoreCaseAndDeletedAtIsNull(normalizedCode)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher với mã: " + normalizedCode));

        repository.giamSoLuotDaDungAtomic(voucher.getId());
    }

    private void validateRequest(KhuyenMaiVoucherRequest r) {
        if (r.maChuongTrinh() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã chương trình không được để trống");
        }
        if (r.maCode() == null || r.maCode().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã voucher không được để trống");
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
        if (r.donHangToiThieu() == null || r.donHangToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn hàng tối thiểu không được âm");
        }
        if (r.mucGiamToiDa() != null && r.mucGiamToiDa().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mức giảm tối đa không được âm");
        }
        if (r.gioiHanSuDung() != null && r.gioiHanSuDung() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Giới hạn sử dụng phải lớn hơn 0");
        }
        if (r.soLuotDaDung() != null && r.soLuotDaDung() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Số lượt đã dùng không được âm");
        }
    }
}
