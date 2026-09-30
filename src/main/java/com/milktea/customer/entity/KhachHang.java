package com.milktea.customer.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "khach_hang") @Getter @Setter @NoArgsConstructor
public class KhachHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "so_dien_thoai", nullable = false, unique = true) private String soDienThoai;
    @Column(name = "ho_va_ten") private String hoVaTen;
}
