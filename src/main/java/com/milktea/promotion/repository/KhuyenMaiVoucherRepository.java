package com.milktea.promotion.repository;

import com.milktea.promotion.entity.KhuyenMaiVoucher;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KhuyenMaiVoucherRepository extends JpaRepository<KhuyenMaiVoucher, Long> {
    Page<KhuyenMaiVoucher> findByDeletedAtIsNull(Pageable pageable);

    Optional<KhuyenMaiVoucher> findByIdAndDeletedAtIsNull(Long id);

    Optional<KhuyenMaiVoucher> findByMaCodeIgnoreCaseAndDeletedAtIsNull(String maCode);

    boolean existsByMaCodeIgnoreCaseAndDeletedAtIsNull(String maCode);

    boolean existsByChuongTrinhIdAndDeletedAtIsNull(Long chuongTrinhId);

    @Modifying
    @Query("UPDATE KhuyenMaiVoucher v SET v.soLuotDaDung = v.soLuotDaDung + 1 WHERE v.id = :id AND (v.gioiHanSuDung IS NULL OR v.soLuotDaDung < v.gioiHanSuDung) AND v.deletedAt IS NULL")
    int tangSoLuotDaDungAtomic(@Param("id") Long id);

    @Modifying
    @Query("UPDATE KhuyenMaiVoucher v SET v.soLuotDaDung = v.soLuotDaDung - 1 WHERE v.id = :id AND v.soLuotDaDung > 0 AND v.deletedAt IS NULL")
    int giamSoLuotDaDungAtomic(@Param("id") Long id);
}
