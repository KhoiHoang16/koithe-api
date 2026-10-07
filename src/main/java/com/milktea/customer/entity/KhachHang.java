package com.milktea.customer.entity;

import com.milktea.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "khach_hang") @Getter @Setter @NoArgsConstructor
public class KhachHang extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "so_dien_thoai", nullable = false, unique = true) private String soDienThoai;
    @Column(name = "ho_va_ten") private String hoVaTen;
    @Column(name = "email") private String email;
    @Column(name = "mat_khau_ma_hoa") private String matKhauMaHoa;
    @Column(name = "diem_tich_luy", nullable = false) private Integer diemTichLuy = 0;
    @Enumerated(EnumType.STRING)
    @Column(name = "hang_thanh_vien", nullable = false) private HangThanhVien hangThanhVien = HangThanhVien.DONG;
    @Column(name = "token_lam_moi") private String tokenLamMoi;
    @Column(name = "da_xac_thuc", nullable = false) private Boolean daXacThuc = false;
    @Column(name = "ngay_tao", nullable = false) private Instant ngayTao = Instant.now();

    @PrePersist
    void applyDefaults() {
        if (diemTichLuy == null) diemTichLuy = 0;
        if (hangThanhVien == null) hangThanhVien = HangThanhVien.DONG;
        if (daXacThuc == null) daXacThuc = false;
        if (ngayTao == null) ngayTao = Instant.now();
    }
}
