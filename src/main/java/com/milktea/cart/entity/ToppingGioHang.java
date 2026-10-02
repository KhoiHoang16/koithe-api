package com.milktea.cart.entity;

import com.milktea.catalog.entity.Topping;
import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ARCHITECTURE NOTE: Cart references catalog through Topping for selected add-ons.
 * This is accepted in the current Modular Monolith; consider a catalog facade or domain events as the boundary grows.
 */
@Entity @Table(name = "topping_gio_hang") @Getter @Setter @NoArgsConstructor
public class ToppingGioHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_chi_tiet_gio", nullable = false) private ChiTietGioHang chiTietGioHang;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_topping", nullable = false) private Topping topping;
    @Column(name = "so_luong", nullable = false) private Integer soLuong = 1;
}
