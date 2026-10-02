package com.milktea.payment.entity;

import com.milktea.common.audit.BaseEntity;
import com.milktea.order.entity.DonHang;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

/**
 * ARCHITECTURE NOTE: Payment references order through DonHang for its payment transaction.
 * This is accepted in the current Modular Monolith; consider an order facade or domain events as the boundary grows.
 */
@Entity
@Table(name = "giao_dich_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
public class GiaoDichThanhToan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang", nullable = false)
    private DonHang donHang;
    @Column(name = "phuong_thuc_thanh_toan", nullable = false)
    private String phuongThucThanhToan;
    @Column(name = "so_tien", nullable = false, precision = 12, scale = 2)
    private BigDecimal soTien;
    @Column(name = "noi_dung_vietqr")
    private String noiDungVietqr;
    @Column(name = "trang_thai", nullable = false)
    private String trangThai;
    @Column(name = "thoi_gian_thanh_toan")
    private Instant thoiGianThanhToan;
}
