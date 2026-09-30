package com.milktea.cart.repository;

import com.milktea.cart.entity.ChiTietGioHang;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, Long> {
    List<ChiTietGioHang> findAllByGioHangIdAndDeletedAtIsNullOrderById(Long cartId);
    Optional<ChiTietGioHang> findByIdAndGioHangIdAndDeletedAtIsNull(Long id, Long cartId);
}
