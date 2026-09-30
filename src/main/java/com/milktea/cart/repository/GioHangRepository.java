package com.milktea.cart.repository;

import com.milktea.cart.entity.GioHang;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GioHangRepository extends JpaRepository<GioHang, Long> {
    Optional<GioHang> findFirstByTokenPhienAndDeletedAtIsNull(String token);
    Optional<GioHang> findFirstByKhachHangIdAndDeletedAtIsNull(Long customerId);
}
