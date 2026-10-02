package com.milktea.inventory.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "nha_cung_cap")
@Getter
@Setter
@NoArgsConstructor
public class NhaCungCap extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ten_nha_cung_cap", nullable = false)
    private String tenNhaCungCap;
    @Column(name = "so_dien_thoai", nullable = false)
    private String soDienThoai;
    @Column(name = "email")
    private String email;
    @Column(name = "dia_chi")
    private String diaChi;
    @Column(name = "ma_so_thue")
    private String maSoThue;
    @Column(name = "nguoi_dai_dien")
    private String nguoiDaiDien;
    @Column(name = "dang_hop_tac", nullable = false)
    private Boolean dangHopTac;
    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao;
}
