package com.milktea.order.entity;

import com.milktea.catalog.entity.Topping;
import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ARCHITECTURE NOTE: Order references catalog through Topping for the ordered topping.
 * This is accepted in the current Modular Monolith; consider a catalog facade or domain events as the boundary grows.
 */
@Entity @Table(name = "topping_don_hang") @Getter @Setter @NoArgsConstructor
public class ToppingDonHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_chi_tiet_don", nullable = false) private ChiTietDonHang chiTietDonHang;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_topping", nullable = false) private Topping topping;
    @Column(name = "so_luong", nullable = false) private Integer soLuong;
    @Column(name = "don_gia", nullable = false, precision = 12, scale = 2) private BigDecimal donGia;
}
