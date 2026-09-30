package com.milktea.order.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "ban") @Getter @Setter @NoArgsConstructor
public class Ban extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "so_ban", nullable = false) private String soBan;
    @Column(name = "ma_qr_token", nullable = false) private String maQrToken;
    @Column(name = "trang_thai", nullable = false) private String trangThai;
}
