package com.milktea.catalog.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bien_the_san_pham", uniqueConstraints = @UniqueConstraint(name = "uk_bien_the_ma_san_pham_kich_co", columnNames = {"ma_san_pham", "kich_co"}))
@Getter @Setter @NoArgsConstructor
public class BienTheSanPham extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_san_pham", nullable = false) private SanPham sanPham;
    @Column(name = "kich_co", nullable = false) private String kichCo;
    @Column(name = "gia_ban", nullable = false, precision = 12, scale = 2) private BigDecimal giaBan;
    @Column(name = "so_luong_ton", nullable = false, precision = 10, scale = 2) private BigDecimal soLuongTon = BigDecimal.ZERO;
    @Column(name = "con_hang", nullable = false) private boolean conHang = true;
    @Version @Column(name = "version", nullable = false) private Long version = 0L;
}
