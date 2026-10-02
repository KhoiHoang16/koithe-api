package com.milktea.shift.entity;

import com.milktea.common.audit.BaseEntity;
import com.milktea.user.entity.NguoiDung;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

/**
 * ARCHITECTURE NOTE: Shift references user through NguoiDung for the assigned cashier.
 * This is accepted in the current Modular Monolith; consider a user facade or domain events as the boundary grows.
 */
@Entity
@Table(name = "ca_lam_viec")
@Getter
@Setter
@NoArgsConstructor
public class CaLamViec extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_thu_ngan", nullable = false)
    private NguoiDung thuNgan;
    @Column(name = "thoi_gian_bat_dau", nullable = false)
    private Instant thoiGianBatDau;
    @Column(name = "thoi_gian_ket_thuc")
    private Instant thoiGianKetThuc;
    @Column(name = "tien_dau_ca", nullable = false, precision = 12, scale = 2)
    private BigDecimal tienDauCa;
    @Column(name = "tien_ket_ca", precision = 12, scale = 2)
    private BigDecimal tienKetCa;
    @Column(name = "trang_thai", nullable = false)
    private String trangThai;
}
