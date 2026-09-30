package com.milktea.order.entity;

import com.milktea.common.audit.BaseEntity;
import com.milktea.customer.entity.KhachHang;
import com.milktea.user.entity.NguoiDung;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "don_hang") @Getter @Setter @NoArgsConstructor
public class DonHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "ma_hien_thi_don", nullable = false, unique = true) private String maHienThiDon;
    @Column(name = "kenh_dat_hang", nullable = false) private String kenhDatHang;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_ca") private CaLamViec caLamViec;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_thu_ngan") private NguoiDung thuNgan;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_khach_hang") private KhachHang khachHang;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_ban") private Ban ban;
    @Column(name = "loai_phuc_vu", nullable = false) private String loaiPhucVu;
    @Column(name = "tong_tien_hang", nullable = false, precision = 12, scale = 2) private BigDecimal tongTienHang;
    @Column(name = "so_tien_giam", nullable = false, precision = 12, scale = 2) private BigDecimal soTienGiam = BigDecimal.ZERO;
    @Column(name = "tong_tien_thanh_toan", nullable = false, precision = 12, scale = 2) private BigDecimal tongTienThanhToan;
    @Column(name = "trang_thai", nullable = false) private String trangThai = "CHO_XAC_NHAN";
    @Column(name = "ngay_tao", nullable = false) private Instant ngayTao = Instant.now();
    @Column(name = "ton_kho_da_tru", nullable = false) private boolean tonKhoDaTru;
    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL) private List<ChiTietDonHang> chiTiet = new ArrayList<>();
}
