package com.milktea.inventory.entity;

import com.milktea.catalog.entity.*;
import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

/**
 * ARCHITECTURE NOTE: Inventory references catalog through BienTheSanPham and Topping stock items.
 * This is accepted in the current Modular Monolith; consider a catalog facade or domain events as the boundary grows.
 */
@Entity
@Table(name = "chi_tiet_phieu_nhap")
@Getter
@Setter
@NoArgsConstructor
public class ChiTietPhieuNhap extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu_nhap", nullable = false)
    private PhieuNhapHang phieuNhapHang;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bien_the")
    private BienTheSanPham bienThe;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_topping")
    private Topping topping;
    @Column(name = "ten_mat_hang", nullable = false)
    private String tenMatHang;
    @Column(name = "don_vi_tinh", nullable = false)
    private String donViTinh;
    @Column(name = "so_luong", nullable = false, precision = 10, scale = 2)
    private BigDecimal soLuong;
    @Column(name = "don_gia_nhap", nullable = false, precision = 12, scale = 2)
    private BigDecimal donGiaNhap;
    @Column(name = "thanh_tien", nullable = false, precision = 12, scale = 2)
    private BigDecimal thanhTien;
}
