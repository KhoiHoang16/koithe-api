package com.milktea.order.repository;

import com.milktea.order.entity.DonHang;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {
    Optional<DonHang> findByIdAndDeletedAtIsNull(Long id);
    @Query("select o from DonHang o where o.deletedAt is null and (:status is null or o.trangThai = :status) and (:channel is null or o.kenhDatHang = :channel)")
    Page<DonHang> search(@Param("status") String status, @Param("channel") String channel, Pageable pageable);
    Page<DonHang> findAllByKhachHangIdAndDeletedAtIsNull(Long customerId, Pageable pageable);

    @Query("select o from DonHang o left join o.khachHang c where o.deletedAt is null and upper(trim(o.maHienThiDon)) = upper(trim(:code)) and ((o.soDienThoaiNguoiNhan is not null and trim(o.soDienThoaiNguoiNhan) = trim(:phone)) or (c.soDienThoai is not null and trim(c.soDienThoai) = trim(:phone)))")
    Optional<DonHang> trackOrder(@Param("code") String code, @Param("phone") String phone);

    @Modifying
    @Query(value = """
            UPDATE giao_dich_thanh_toan
            SET trang_thai = 'THANH_CONG',
                thoi_gian_thanh_toan = COALESCE(thoi_gian_thanh_toan, CURRENT_TIMESTAMP)
            WHERE ma_don_hang = :orderId
              AND deleted_at IS NULL
              AND phuong_thuc_thanh_toan = 'TIEN_MAT'
              AND trang_thai = 'CHO_XU_LY'
            """, nativeQuery = true)
    int markPendingCashPaymentSuccess(@Param("orderId") Long orderId);
}
