package com.milktea.promotion.entity;

import com.milktea.catalog.entity.*;
import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

/**
 * ARCHITECTURE NOTE: Promotion references catalog through SanPham and LoaiSanPham targets.
 * This is accepted in the current Modular Monolith; consider a catalog facade or domain events as the boundary grows.
 */
@Entity
@Table(name = "khuyen_mai_san_pham")
@Getter
@Setter
@NoArgsConstructor
public class KhuyenMaiSanPham extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuong_trinh", nullable = false)
    private ChuongTrinhKhuyenMai chuongTrinh;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham")
    private SanPham sanPham;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_muc")
    private LoaiSanPham danhMuc;
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_giam_gia", nullable = false)
    private LoaiGiamGia loaiGiamGia;
    @Column(name = "gia_tri_giam", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaTriGiam;
}
