package com.milktea.promotion.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "chuong_trinh_khuyen_mai")
@Getter
@Setter
@NoArgsConstructor
public class ChuongTrinhKhuyenMai extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ten_chuong_trinh", nullable = false)
    private String tenChuongTrinh;
    @Column(name = "mo_ta")
    private String moTa;
    @Column(name = "ngay_bat_dau", nullable = false)
    private Instant ngayBatDau;
    @Column(name = "ngay_ket_thuc", nullable = false)
    private Instant ngayKetThuc;
    @Column(name = "dang_hoat_dong", nullable = false)
    private Boolean dangHoatDong;
}
