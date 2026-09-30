package com.milktea.user.entity;
import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="vai_tro") @Getter @Setter @NoArgsConstructor
public class VaiTro extends BaseEntity { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="ten_vai_tro") private String tenVaiTro; @Column(name="mo_ta") private String moTa; }
