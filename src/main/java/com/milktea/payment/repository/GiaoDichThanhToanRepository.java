package com.milktea.payment.repository;

import com.milktea.payment.entity.GiaoDichThanhToan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiaoDichThanhToanRepository extends JpaRepository<GiaoDichThanhToan, Long> {
    Optional<GiaoDichThanhToan> findByIdAndDeletedAtIsNull(Long id);
    List<GiaoDichThanhToan> findAllByDeletedAtIsNull();
    List<GiaoDichThanhToan> findAllByDonHangIdAndDeletedAtIsNull(Long maDonHang);
}
