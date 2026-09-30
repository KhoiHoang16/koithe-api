package com.milktea.catalog.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loai_san_pham")
@Getter @Setter @NoArgsConstructor
public class LoaiSanPham extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "ten_danh_muc", nullable = false) private String tenDanhMuc;
}
