package com.milktea.catalog.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "san_pham")
@Getter @Setter @NoArgsConstructor
public class SanPham extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_danh_muc", nullable = false) private LoaiSanPham danhMuc;
    @Column(name = "ten_san_pham", nullable = false) private String tenSanPham;
    @Column(name = "mo_ta") private String moTa;
}
