package com.milktea.order.repository;

import com.milktea.order.entity.ChiTietDonHang;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Long> {
    List<ChiTietDonHang> findAllByDonHangIdAndDeletedAtIsNullOrderById(Long orderId);
}
