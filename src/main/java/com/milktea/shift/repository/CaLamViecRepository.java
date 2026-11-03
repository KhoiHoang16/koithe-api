package com.milktea.shift.repository;

import com.milktea.shift.entity.CaLamViec;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaLamViecRepository extends JpaRepository<CaLamViec, Long> {
    Optional<CaLamViec> findByIdAndDeletedAtIsNull(Long id);

    List<CaLamViec> findAllByDeletedAtIsNullOrderByThoiGianBatDauDesc();

    List<CaLamViec> findAllByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc(String trangThai);

    Optional<CaLamViec> findFirstByTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc(String trangThai);

    Optional<CaLamViec> findFirstByThuNganIdAndTrangThaiAndDeletedAtIsNullOrderByThoiGianBatDauDesc(Long thuNganId,
            String trangThai);

    boolean existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(Long thuNganId, String trangThai);

    @Query(value = "SELECT COALESCE(SUM(d.tong_tien_thanh_toan), 0) FROM don_hang d WHERE d.ma_ca = :caId AND d.deleted_at IS NULL AND d.trang_thai != 'DA_HUY'", nativeQuery = true)
    BigDecimal sumRevenueByCaId(@Param("caId") Long caId);

    @Query(value = "SELECT COUNT(*) FROM don_hang d WHERE d.ma_ca = :caId AND d.deleted_at IS NULL AND d.trang_thai != 'DA_HUY'", nativeQuery = true)
    Long countOrdersByCaId(@Param("caId") Long caId);

    @Query(value = """
            SELECT COALESCE(SUM(g.so_tien), 0)
            FROM giao_dich_thanh_toan g
            JOIN don_hang d ON g.ma_don_hang = d.id
            WHERE d.ma_ca = :caId
              AND d.deleted_at IS NULL
              AND g.deleted_at IS NULL
              AND g.phuong_thuc_thanh_toan = 'TIEN_MAT'
              AND g.trang_thai = 'THANH_CONG'
            """, nativeQuery = true)
    BigDecimal sumTienMatThanhCongByCaId(@Param("caId") Long caId);

    @Modifying
    @Query(value = """
            UPDATE don_hang
            SET ma_ca = :caId,
                ma_thu_ngan = COALESCE(ma_thu_ngan, :thuNganId)
            WHERE ma_ca IS NULL
              AND deleted_at IS NULL
              AND trang_thai != 'DA_HUY'
            """, nativeQuery = true)
    int claimUnassignedOrders(@Param("caId") Long caId, @Param("thuNganId") Long thuNganId);
}
