package com.milktea.customer.repository;

import com.milktea.customer.entity.KhachHang;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findBySoDienThoaiAndDeletedAtIsNull(String phone);
    Optional<KhachHang> findByIdAndDeletedAtIsNull(Long id);
    boolean existsBySoDienThoaiAndDeletedAtIsNull(String phone);
    List<KhachHang> findAllByDeletedAtIsNull();
    Optional<KhachHang> findBySoDienThoai(String phone);
}
