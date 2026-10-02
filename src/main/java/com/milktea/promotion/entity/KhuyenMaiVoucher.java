package com.milktea.promotion.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "khuyen_mai_voucher")
@Getter
@Setter
@NoArgsConstructor
public class KhuyenMaiVoucher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chuong_trinh", nullable = false)
    private ChuongTrinhKhuyenMai chuongTrinh;
    @Column(name = "ma_code", nullable = false, unique = true)
    private String maCode;
    @Column(name = "loai_giam_gia", nullable = false)
    private String loaiGiamGia;
    @Column(name = "gia_tri_giam", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaTriGiam;
    @Column(name = "muc_giam_toi_da", precision = 12, scale = 2)
    private BigDecimal mucGiamToiDa;
    @Column(name = "don_hang_toi_thieu", nullable = false, precision = 12, scale = 2)
    private BigDecimal donHangToiThieu;
    @Column(name = "gioi_han_su_dung")
    private Integer gioiHanSuDung;
    @Column(name = "so_luot_da_dung", nullable = false)
    private Integer soLuotDaDung;
}
