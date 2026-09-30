package com.milktea.cart.repository;

import com.milktea.cart.entity.ToppingGioHang;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToppingGioHangRepository extends JpaRepository<ToppingGioHang, Long> {
    List<ToppingGioHang> findAllByChiTietGioHangIdAndDeletedAtIsNull(Long itemId);
}
