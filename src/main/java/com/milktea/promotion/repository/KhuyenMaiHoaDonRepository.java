package com.milktea.promotion.repository;

import com.milktea.promotion.entity.KhuyenMaiHoaDon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KhuyenMaiHoaDonRepository extends JpaRepository<KhuyenMaiHoaDon, Long> {
    Page<KhuyenMaiHoaDon> findByDeletedAtIsNull(Pageable pageable);

    Optional<KhuyenMaiHoaDon> findByIdAndDeletedAtIsNull(Long id);

    List<KhuyenMaiHoaDon> findByChuongTrinhIdAndDeletedAtIsNull(Long chuongTrinhId);

    boolean existsByChuongTrinhIdAndDeletedAtIsNull(Long chuongTrinhId);

    @Query("SELECT k FROM KhuyenMaiHoaDon k JOIN FETCH k.chuongTrinh c WHERE k.deletedAt IS NULL AND c.deletedAt IS NULL AND c.dangHoatDong = true")
    List<KhuyenMaiHoaDon> findAllActiveWithProgram();
}
