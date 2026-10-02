package com.milktea.order.entity;

import com.milktea.common.audit.BaseEntity;
import com.milktea.customer.entity.KhachHang;
import com.milktea.promotion.entity.KhuyenMaiVoucher;
import com.milktea.shift.entity.CaLamViec;
import com.milktea.table.entity.Ban;
import com.milktea.user.entity.NguoiDung;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ARCHITECTURE NOTE: Order references customer, shift, table, user, and promotion entities through JPA relations.
 * These cross-module references are accepted in the current Modular Monolith; consider owning-module facades or
 * domain events as workflows grow. Keep the module dependency explicit; no refactor is required now.
 */
@Entity
@Table(name = "don_hang")
@Getter
@Setter
@NoArgsConstructor
public class DonHang extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ma_hien_thi_don", nullable = false, unique = true)
    private String maHienThiDon;
    @Column(name = "kenh_dat_hang", nullable = false)
    private String kenhDatHang;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_ca")
    private CaLamViec caLamViec;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_thu_ngan")
    private NguoiDung thuNgan;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach_hang")
    private KhachHang khachHang;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_ban")
    private Ban ban;
    // ARCHITECTURE NOTE: Dependency from order -> promotion through the voucher entity.
    // As promotion workflows grow, consider a domain event or a public facade interface
    // instead of importing another module's persistence entity directly.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_voucher")
    private KhuyenMaiVoucher voucher;
    @Column(name = "loai_phuc_vu", nullable = false)
    private String loaiPhucVu;
    @Column(name = "tong_tien_hang", nullable = false, precision = 12, scale = 2)
    private BigDecimal tongTienHang;
    @Column(name = "so_tien_giam", nullable = false, precision = 12, scale = 2)
    private BigDecimal soTienGiam = BigDecimal.ZERO;
    @Column(name = "tong_tien_thanh_toan", nullable = false, precision = 12, scale = 2)
    private BigDecimal tongTienThanhToan;
    @Column(name = "trang_thai", nullable = false)
    private String trangThai = "CHO_XAC_NHAN";
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao = Instant.now();
    @Column(name = "ton_kho_da_tru", nullable = false)
    private boolean tonKhoDaTru;
    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL)
    private List<ChiTietDonHang> chiTiet = new ArrayList<>();
}
