package com.milktea.cart.entity;

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
 * ARCHITECTURE NOTE: Cart references catalog through BienTheSanPham for selected products.
 * This is accepted in the current Modular Monolith; consider a catalog facade or domain events as the boundary grows.
 */
@Entity @Table(name = "chi_tiet_gio_hang") @Getter @Setter @NoArgsConstructor
public class ChiTietGioHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_gio_hang", nullable = false) private GioHang gioHang;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_bien_the", nullable = false) private BienTheSanPham bienThe;
    @Column(name = "so_luong", nullable = false) private Integer soLuong = 1;
    @Column(name = "muc_duong") private String mucDuong;
    @Column(name = "muc_da") private String mucDa;
    @Column(name = "ghi_chu_mon") private String ghiChuMon;
    @Column(name = "thanh_tien", nullable = false, precision = 12, scale = 2) private BigDecimal thanhTien = BigDecimal.ZERO;
    @OneToMany(mappedBy = "chiTietGioHang", cascade = CascadeType.ALL) private List<ToppingGioHang> toppings = new ArrayList<>();
}
