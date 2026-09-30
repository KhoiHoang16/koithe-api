package com.milktea.order.repository;

import com.milktea.order.entity.ToppingDonHang;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToppingDonHangRepository extends JpaRepository<ToppingDonHang, Long> {
    List<ToppingDonHang> findAllByChiTietDonHangIdAndDeletedAtIsNull(Long orderItemId);
}
