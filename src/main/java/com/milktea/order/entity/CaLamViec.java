package com.milktea.order.entity;

import com.milktea.common.audit.BaseEntity;
import com.milktea.user.entity.NguoiDung;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "ca_lam_viec") @Getter @Setter @NoArgsConstructor
public class CaLamViec extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ma_thu_ngan", nullable = false) private NguoiDung thuNgan;
    @Column(name = "trang_thai", nullable = false) private String trangThai;
}
