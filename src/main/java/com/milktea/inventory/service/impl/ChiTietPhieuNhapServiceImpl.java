package com.milktea.inventory.service.impl;

import com.milktea.catalog.entity.BienTheSanPham;
import com.milktea.catalog.entity.Topping;
import com.milktea.catalog.repository.BienTheSanPhamRepository;
import com.milktea.catalog.repository.ToppingRepository;
import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.inventory.dto.ChiTietPhieuNhapRequest;
import com.milktea.inventory.dto.ChiTietPhieuNhapResponse;
import com.milktea.inventory.entity.ChiTietPhieuNhap;
import com.milktea.inventory.entity.PhieuNhapHang;
import com.milktea.inventory.mapper.ChiTietPhieuNhapMapper;
import com.milktea.inventory.repository.ChiTietPhieuNhapRepository;
import com.milktea.inventory.repository.PhieuNhapHangRepository;
import com.milktea.inventory.service.ChiTietPhieuNhapService;
import java.math.BigDecimal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChiTietPhieuNhapServiceImpl implements ChiTietPhieuNhapService {

    private static final String CHO_DUYET = "CHO_DUYET";

    private final ChiTietPhieuNhapRepository repository;
    private final PhieuNhapHangRepository phieuNhapHangRepository;
    private final BienTheSanPhamRepository bienTheRepository;
    private final ToppingRepository toppingRepository;
    private final ChiTietPhieuNhapMapper mapper;

    public ChiTietPhieuNhapServiceImpl(
            ChiTietPhieuNhapRepository repository,
            PhieuNhapHangRepository phieuNhapHangRepository,
            BienTheSanPhamRepository bienTheRepository,
            ToppingRepository toppingRepository,
            ChiTietPhieuNhapMapper mapper) {
        this.repository = repository;
        this.phieuNhapHangRepository = phieuNhapHangRepository;
        this.bienTheRepository = bienTheRepository;
        this.toppingRepository = toppingRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<ChiTietPhieuNhapResponse> getAllByMaPhieuNhap(Long maPhieuNhap, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return PageResponse.from(repository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(maPhieuNhap, pageable)
                .map(mapper::toResponse));
    }

    @Override
    public ChiTietPhieuNhapResponse getById(Long id) {
        return mapper.toResponse(line(id));
    }

    @Override
    @Transactional
    public ChiTietPhieuNhapResponse create(ChiTietPhieuNhapRequest r) {
        if (r.maPhieuNhap() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã phiếu nhập không được để trống");
        }

        PhieuNhapHang phieu = phieuNhapHangRepository.findByIdAndDeletedAtIsNull(r.maPhieuNhap())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu nhập hàng"));

        if (!CHO_DUYET.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Chỉ có thể thêm mặt hàng vào phiếu nhập đang chờ duyệt");
        }

        validateLineItemRequest(r);

        BienTheSanPham bienThe = null;
        if (r.maBienThe() != null) {
            bienThe = bienTheRepository.findByIdAndDeletedAtIsNull(r.maBienThe())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể sản phẩm"));
        }

        Topping topping = null;
        if (r.maTopping() != null) {
            topping = toppingRepository.findByIdAndDeletedAtIsNull(r.maTopping())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy topping"));
        }

        ChiTietPhieuNhap line = new ChiTietPhieuNhap();
        line.setPhieuNhapHang(phieu);
        line.setBienThe(bienThe);
        line.setTopping(topping);

        String tenMatHang = r.tenMatHang();
        if (tenMatHang == null || tenMatHang.isBlank()) {
            if (bienThe != null) {
                tenMatHang = bienThe.getSanPham().getTenSanPham() + " (" + bienThe.getKichCo() + ")";
            } else if (topping != null) {
                tenMatHang = topping.getTenTopping();
            } else {
                tenMatHang = "Mặt hàng";
            }
        }
        line.setTenMatHang(tenMatHang);
        line.setDonViTinh(r.donViTinh() != null && !r.donViTinh().isBlank() ? r.donViTinh().trim() : "Phần");
        line.setSoLuong(r.soLuong());
        line.setDonGiaNhap(r.donGiaNhap());
        line.setThanhTien(r.soLuong().multiply(r.donGiaNhap()));

        ChiTietPhieuNhap saved = repository.save(line);
        recalculateAndSaveTotal(phieu);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ChiTietPhieuNhapResponse update(Long id, ChiTietPhieuNhapRequest r) {
        ChiTietPhieuNhap line = line(id);
        PhieuNhapHang phieu = line.getPhieuNhapHang();

        if (!CHO_DUYET.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Chỉ có thể chỉnh sửa mặt hàng trong phiếu nhập đang chờ duyệt");
        }

        if (r.soLuong() != null) {
            if (r.soLuong().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Số lượng nhập phải lớn hơn 0");
            }
            line.setSoLuong(r.soLuong());
        }

        if (r.donGiaNhap() != null) {
            if (r.donGiaNhap().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn giá nhập không được âm");
            }
            line.setDonGiaNhap(r.donGiaNhap());
        }

        if (r.tenMatHang() != null && !r.tenMatHang().isBlank()) {
            line.setTenMatHang(r.tenMatHang().trim());
        }
        if (r.donViTinh() != null && !r.donViTinh().isBlank()) {
            line.setDonViTinh(r.donViTinh().trim());
        }

        line.setThanhTien(line.getSoLuong().multiply(line.getDonGiaNhap()));
        recalculateAndSaveTotal(phieu);

        return mapper.toResponse(line);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ChiTietPhieuNhap line = line(id);
        PhieuNhapHang phieu = line.getPhieuNhapHang();

        if (!CHO_DUYET.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Chỉ có thể xóa mặt hàng trong phiếu nhập đang chờ duyệt");
        }

        line.markDeleted();
        recalculateAndSaveTotal(phieu);
    }

    private ChiTietPhieuNhap line(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy dòng chi tiết phiếu nhập"));
    }

    private void validateLineItemRequest(ChiTietPhieuNhapRequest r) {
        if (r.soLuong() == null || r.soLuong().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Số lượng nhập phải lớn hơn 0");
        }
        if (r.donGiaNhap() == null || r.donGiaNhap().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn giá nhập không được âm");
        }
        if (r.maBienThe() == null && r.maTopping() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cần chọn biến thể sản phẩm hoặc topping");
        }
    }

    private void recalculateAndSaveTotal(PhieuNhapHang phieu) {
        BigDecimal total = repository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(phieu.getId())
                .stream()
                .map(ChiTietPhieuNhap::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        phieu.setTongTien(total);
        phieuNhapHangRepository.save(phieu);
    }
}
