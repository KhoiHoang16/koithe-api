package com.milktea.order.repository;

import com.milktea.order.entity.DonHang;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {
    Optional<DonHang> findByIdAndDeletedAtIsNull(Long id);
    @Query("select o from DonHang o where o.deletedAt is null and (:status is null or o.trangThai = :status) and (:channel is null or o.kenhDatHang = :channel)")
    Page<DonHang> search(@Param("status") String status, @Param("channel") String channel, Pageable pageable);
    Page<DonHang> findAllByKhachHangIdAndDeletedAtIsNull(Long customerId, Pageable pageable);
}
