package com.milktea.inventory.repository;

import com.milktea.inventory.entity.PhieuNhapHang;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhieuNhapHangRepository extends JpaRepository<PhieuNhapHang, Long> {
    Optional<PhieuNhapHang> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByMaPhieuNhapAndDeletedAtIsNull(String maPhieuNhap);

    boolean existsByNhaCungCapIdAndDeletedAtIsNull(Long nhaCungCapId);

    @Query("SELECT p FROM PhieuNhapHang p WHERE p.deletedAt IS NULL " +
           "AND (:supplierId IS NULL OR p.nhaCungCap.id = :supplierId) " +
           "AND (:trangThai IS NULL OR p.trangThai = :trangThai) " +
           "AND (CAST(:fromDate AS timestamp) IS NULL OR p.ngayNhap >= :fromDate) " +
           "AND (CAST(:toDate AS timestamp) IS NULL OR p.ngayNhap <= :toDate)")
    Page<PhieuNhapHang> search(
            @Param("supplierId") Long supplierId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("trangThai") String trangThai,
            Pageable pageable);
}
