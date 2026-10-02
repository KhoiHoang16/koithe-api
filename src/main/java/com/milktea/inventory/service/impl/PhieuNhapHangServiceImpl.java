package com.milktea.inventory.service.impl;

import com.milktea.catalog.entity.BienTheSanPham;
import com.milktea.catalog.entity.Topping;
import com.milktea.catalog.repository.BienTheSanPhamRepository;
import com.milktea.catalog.repository.ToppingRepository;
import com.milktea.common.exception.BusinessException;
import com.milktea.common.response.PageResponse;
import com.milktea.inventory.dto.*;
import com.milktea.inventory.entity.ChiTietPhieuNhap;
import com.milktea.inventory.entity.NhaCungCap;
import com.milktea.inventory.entity.PhieuNhapHang;
import com.milktea.inventory.mapper.PhieuNhapHangMapper;
import com.milktea.inventory.repository.ChiTietPhieuNhapRepository;
import com.milktea.inventory.repository.NhaCungCapRepository;
import com.milktea.inventory.repository.PhieuNhapHangRepository;
import com.milktea.inventory.service.PhieuNhapHangService;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.repository.NguoiDungRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PhieuNhapHangServiceImpl implements PhieuNhapHangService {

    public static final String CHO_DUYET = "CHO_DUYET";
    public static final String DA_NHAP_KHO = "DA_NHAP_KHO";
    public static final String DA_HUY = "DA_HUY";

    private final PhieuNhapHangRepository repository;
    private final ChiTietPhieuNhapRepository chiTietRepository;
    private final NhaCungCapRepository supplierRepository;
    private final NguoiDungRepository userRepository;
    private final BienTheSanPhamRepository bienTheRepository;
    private final ToppingRepository toppingRepository;
    private final PhieuNhapHangMapper mapper;

    public PhieuNhapHangServiceImpl(
            PhieuNhapHangRepository repository,
            ChiTietPhieuNhapRepository chiTietRepository,
            NhaCungCapRepository supplierRepository,
            NguoiDungRepository userRepository,
            BienTheSanPhamRepository bienTheRepository,
            ToppingRepository toppingRepository,
            PhieuNhapHangMapper mapper) {
        this.repository = repository;
        this.chiTietRepository = chiTietRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.bienTheRepository = bienTheRepository;
        this.toppingRepository = toppingRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<PhieuNhapHangResponse> getAll(
            Long supplierId, Instant fromDate, Instant toDate, String trangThai, int page, int size) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Khoảng thời gian không hợp lệ: từ ngày phải trước đến ngày");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayNhap").descending());
        return PageResponse.from(repository.search(supplierId, fromDate, toDate, trangThai, pageable)
                .map(mapper::toResponse));
    }

    @Override
    public PhieuNhapHangResponse getById(Long id) {
        return mapper.toResponse(purchaseOrder(id));
    }

    @Override
    @Transactional
    public PhieuNhapHangResponse create(PhieuNhapHangRequest r) {
        if (r.maNhaCungCap() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã nhà cung cấp không được để trống");
        }
        if (r.maNguoiNhap() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã người nhập không được để trống");
        }

        NhaCungCap ncc = supplierRepository.findByIdAndDeletedAtIsNull(r.maNhaCungCap())
                .filter(s -> Boolean.TRUE.equals(s.getDangHopTac()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp hoặc nhà cung cấp đã dừng hợp tác"));

        NguoiDung nguoiNhap = userRepository.findById(r.maNguoiNhap())
                .filter(NguoiDung::isDangHoatDong)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người nhập hoặc tài khoản đã bị vô hiệu hóa"));

        String maPhieu = r.maPhieuNhap();
        if (maPhieu != null && !maPhieu.isBlank()) {
            maPhieu = maPhieu.trim();
            if (repository.existsByMaPhieuNhapAndDeletedAtIsNull(maPhieu)) {
                throw new BusinessException(HttpStatus.CONFLICT, "Mã phiếu nhập đã tồn tại");
            }
        } else {
            maPhieu = "PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        PhieuNhapHang phieu = new PhieuNhapHang();
        phieu.setMaPhieuNhap(maPhieu);
        phieu.setNhaCungCap(ncc);
        phieu.setNguoiNhap(nguoiNhap);
        phieu.setGhiChu(r.ghiChu() != null ? r.ghiChu().trim() : null);
        phieu.setTrangThai(CHO_DUYET);
        phieu.setNgayNhap(r.ngayNhap() != null ? r.ngayNhap() : Instant.now());
        phieu.setTongTien(BigDecimal.ZERO);

        PhieuNhapHang savedPhieu = repository.save(phieu);

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (r.chiTiet() != null && !r.chiTiet().isEmpty()) {
            for (ChiTietPhieuNhapRequest lineReq : r.chiTiet()) {
                validateLineItem(lineReq);

                BienTheSanPham bienThe = null;
                if (lineReq.maBienThe() != null) {
                    bienThe = bienTheRepository.findByIdAndDeletedAtIsNull(lineReq.maBienThe())
                            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể sản phẩm"));
                }

                Topping topping = null;
                if (lineReq.maTopping() != null) {
                    topping = toppingRepository.findByIdAndDeletedAtIsNull(lineReq.maTopping())
                            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy topping"));
                }

                ChiTietPhieuNhap line = new ChiTietPhieuNhap();
                line.setPhieuNhapHang(savedPhieu);
                line.setBienThe(bienThe);
                line.setTopping(topping);

                String tenMatHang = lineReq.tenMatHang();
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
                line.setDonViTinh(lineReq.donViTinh() != null && !lineReq.donViTinh().isBlank() ? lineReq.donViTinh().trim() : "Phần");
                line.setSoLuong(lineReq.soLuong());
                line.setDonGiaNhap(lineReq.donGiaNhap());

                BigDecimal lineTotal = lineReq.soLuong().multiply(lineReq.donGiaNhap());
                line.setThanhTien(lineTotal);
                chiTietRepository.save(line);

                calculatedTotal = calculatedTotal.add(lineTotal);
            }
        }

        savedPhieu.setTongTien(calculatedTotal);
        return mapper.toResponse(repository.save(savedPhieu));
    }

    @Override
    @Transactional
    public PhieuNhapHangResponse update(Long id, PhieuNhapHangRequest r) {
        PhieuNhapHang phieu = purchaseOrder(id);

        if (!CHO_DUYET.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Chỉ có thể chỉnh sửa phiếu nhập ở trạng thái chờ duyệt");
        }

        if (r.maNhaCungCap() != null) {
            NhaCungCap ncc = supplierRepository.findByIdAndDeletedAtIsNull(r.maNhaCungCap())
                    .filter(s -> Boolean.TRUE.equals(s.getDangHopTac()))
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp hoặc nhà cung cấp đã dừng hợp tác"));
            phieu.setNhaCungCap(ncc);
        }

        if (r.maNguoiNhap() != null) {
            NguoiDung nguoiNhap = userRepository.findById(r.maNguoiNhap())
                    .filter(NguoiDung::isDangHoatDong)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người nhập hoặc tài khoản đã bị vô hiệu hóa"));
            phieu.setNguoiNhap(nguoiNhap);
        }

        if (r.ghiChu() != null) {
            phieu.setGhiChu(r.ghiChu().trim());
        }

        return mapper.toResponse(phieu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PhieuNhapHang phieu = purchaseOrder(id);

        if (!CHO_DUYET.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Chỉ có thể hủy phiếu nhập ở trạng thái chờ duyệt");
        }

        phieu.setTrangThai(DA_HUY);
        phieu.markDeleted();

        List<ChiTietPhieuNhap> lines = chiTietRepository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(id);
        lines.forEach(ChiTietPhieuNhap::markDeleted);
    }

    @Override
    @Transactional
    public PhieuNhapHangResponse approve(Long id) {
        PhieuNhapHang phieu = purchaseOrder(id);

        if (DA_NHAP_KHO.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Phiếu nhập hàng đã được duyệt trước đó");
        }

        if (DA_HUY.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Không thể duyệt phiếu nhập đã bị hủy");
        }

        if (!CHO_DUYET.equals(phieu.getTrangThai())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Phiếu nhập không ở trạng thái chờ duyệt");
        }

        List<ChiTietPhieuNhap> lines = chiTietRepository.findAllByPhieuNhapHangIdAndDeletedAtIsNull(id);
        if (lines.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Phiếu nhập không có mặt hàng nào để nhập kho");
        }

        for (ChiTietPhieuNhap line : lines) {
            if (line.getBienThe() != null) {
                BienTheSanPham bt = bienTheRepository.findByIdAndDeletedAtIsNull(line.getBienThe().getId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy biến thể sản phẩm"));
                BigDecimal currentStock = bt.getSoLuongTon() != null ? bt.getSoLuongTon() : BigDecimal.ZERO;
                BigDecimal newStock = currentStock.add(line.getSoLuong());
                bt.setSoLuongTon(newStock);
                bt.setConHang(newStock.compareTo(BigDecimal.ZERO) > 0);
                bienTheRepository.save(bt);
            } else if (line.getTopping() != null) {
                Topping tp = toppingRepository.findByIdAndDeletedAtIsNull(line.getTopping().getId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy topping"));
                BigDecimal currentStock = tp.getSoLuongTon() != null ? tp.getSoLuongTon() : BigDecimal.ZERO;
                BigDecimal newStock = currentStock.add(line.getSoLuong());
                tp.setSoLuongTon(newStock);
                tp.setConHang(newStock.compareTo(BigDecimal.ZERO) > 0);
                toppingRepository.save(tp);
            }
        }

        phieu.setTrangThai(DA_NHAP_KHO);
        return mapper.toResponse(repository.save(phieu));
    }

    private PhieuNhapHang purchaseOrder(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu nhập hàng"));
    }

    private void validateLineItem(ChiTietPhieuNhapRequest lineReq) {
        if (lineReq.soLuong() == null || lineReq.soLuong().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Số lượng mặt hàng phải lớn hơn 0");
        }
        if (lineReq.donGiaNhap() == null || lineReq.donGiaNhap().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Đơn giá nhập không được âm");
        }
        if (lineReq.maBienThe() == null && lineReq.maTopping() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cần chọn biến thể sản phẩm hoặc topping");
        }
    }
}
