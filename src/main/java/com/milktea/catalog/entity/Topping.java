package com.milktea.catalog.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "topping")
@Getter @Setter @NoArgsConstructor
public class Topping extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "ten_topping", nullable = false) private String tenTopping;
    @Column(name = "gia_ban", nullable = false, precision = 12, scale = 2) private BigDecimal giaBan;
    @Column(name = "so_luong_ton", nullable = false, precision = 10, scale = 2) private BigDecimal soLuongTon = BigDecimal.ZERO;
    @Column(name = "con_hang", nullable = false) private boolean conHang = true;
    @Version @Column(name = "version", nullable = false) private Long version = 0L;
}
