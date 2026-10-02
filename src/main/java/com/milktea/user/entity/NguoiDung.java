package com.milktea.user.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
public class NguoiDung extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_vai_tro")
    private VaiTro vaiTro;
    @Column(name = "ten_dang_nhap")
    private String tenDangNhap;
    @Column(name = "mat_khau_ma_hoa")
    private String matKhauMaHoa;
    @Column(name = "ho_va_ten")
    private String hoVaTen;
    @Column(name = "so_dien_thoai")
    private String soDienThoai;
    @Column(name = "dang_hoat_dong")
    private boolean dangHoatDong;
    @Column(name = "ngay_tao")
    private Instant ngayTao;
}
