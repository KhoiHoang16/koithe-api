package com.milktea.customer.repository;

import com.milktea.customer.entity.KhachHang;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findBySoDienThoai(String phone);
}
