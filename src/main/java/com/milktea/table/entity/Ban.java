package com.milktea.table.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ban")
@Getter
@Setter
@NoArgsConstructor
public class Ban extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "so_ban", nullable = false, unique = true)
    private String soBan;
    @Column(name = "ma_qr_token", nullable = false, unique = true)
    private String maQrToken;
    @Column(name = "trang_thai", nullable = false)
    private String trangThai;
}
