package com.milktea.cart.entity;

import com.milktea.common.audit.BaseEntity;
import com.milktea.customer.entity.KhachHang;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ARCHITECTURE NOTE: Cart references customer through KhachHang for an authenticated customer's cart.
 * This is accepted in the current Modular Monolith; consider a customer facade or domain events as the boundary grows.
 */
@Entity @Table(name = "gio_hang") @Getter @Setter @NoArgsConstructor
public class GioHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_khach_hang") private KhachHang khachHang;
    @Column(name = "token_phien", nullable = false) private String tokenPhien;
    @Column(name = "thoi_gian_cap_nhat", nullable = false) private Instant thoiGianCapNhat = Instant.now();
    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL) private List<ChiTietGioHang> chiTiet = new ArrayList<>();
}
