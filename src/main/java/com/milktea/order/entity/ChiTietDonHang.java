package com.milktea.order.entity;

import com.milktea.catalog.entity.BienTheSanPham;
import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ARCHITECTURE NOTE: Order references catalog through BienTheSanPham for the ordered item.
 * This is accepted in the current Modular Monolith; consider a catalog facade or domain events as the boundary grows.
 */
@Entity @Table(name = "chi_tiet_don_hang") @Getter @Setter @NoArgsConstructor
public class ChiTietDonHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_don_hang", nullable = false) private DonHang donHang;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_bien_the", nullable = false) private BienTheSanPham bienThe;
    @Column(name = "so_luong", nullable = false) private Integer soLuong;
    @Column(name = "don_gia", nullable = false, precision = 12, scale = 2) private BigDecimal donGia;
    @Column(name = "muc_duong") private String mucDuong;
    @Column(name = "muc_da") private String mucDa;
    @Column(name = "ghi_chu_mon") private String ghiChuMon;
    @Column(name = "thanh_tien", nullable = false, precision = 12, scale = 2) private BigDecimal thanhTien;
    @OneToMany(mappedBy = "chiTietDonHang", cascade = CascadeType.ALL) private List<ToppingDonHang> toppings = new ArrayList<>();
}
