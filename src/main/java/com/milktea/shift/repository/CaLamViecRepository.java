package com.milktea.shift.repository;

import com.milktea.shift.entity.CaLamViec;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaLamViecRepository extends JpaRepository<CaLamViec, Long> {
    Optional<CaLamViec> findByIdAndDeletedAtIsNull(Long id);

    List<CaLamViec> findAllByDeletedAtIsNullOrderByThoiGianBatDauDesc();

    List<CaLamViec> findAllByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc(String trangThai);

    Optional<CaLamViec> findFirstByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc(String trangThai);

    Optional<CaLamViec> findFirstByThuNganIdAndTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc(Long thuNganId, String trangThai);

    boolean existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(Long thuNganId, String trangThai);

    @Query(value = "SELECT COALESCE(SUM(d.tong_tien_thanh_toan), 0) FROM don_hang d WHERE d.ma_ca = :caId AND d.deleted_at IS NULL", nativeQuery = true)
    BigDecimal sumRevenueByCaId(@Param("caId") Long caId);

    @Query(value = "SELECT COUNT(*) FROM don_hang d WHERE d.ma_ca = :caId AND d.deleted_at IS NULL", nativeQuery = true)
    Long countOrdersByCaId(@Param("caId") Long caId);
}
