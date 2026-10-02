package com.milktea.inventory.entity;

import com.milktea.common.audit.BaseEntity;
import com.milktea.user.entity.NguoiDung;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

/**
 * ARCHITECTURE NOTE: Inventory references user through NguoiDung for the receiving employee.
 * This is accepted in the current Modular Monolith; consider a user facade or domain events as the boundary grows.
 */
@Entity
@Table(name = "phieu_nhap_hang")
@Getter
@Setter
@NoArgsConstructor
public class PhieuNhapHang extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ma_phieu_nhap", nullable = false, unique = true)
    private String maPhieuNhap;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nha_cung_cap", nullable = false)
    private NhaCungCap nhaCungCap;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_nhap", nullable = false)
    private NguoiDung nguoiNhap;
    @Column(name = "tong_tien", nullable = false, precision = 12, scale = 2)
    private BigDecimal tongTien;
    @Column(name = "ghi_chu")
    private String ghiChu;
    @Column(name = "trang_thai", nullable = false)
    private String trangThai;
    @Column(name = "ngay_nhap", nullable = false)
    private Instant ngayNhap;
}
