package com.milktea.promotion.repository;

import com.milktea.promotion.entity.ChuongTrinhKhuyenMai;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChuongTrinhKhuyenMaiRepository extends JpaRepository<ChuongTrinhKhuyenMai, Long> {
    Page<ChuongTrinhKhuyenMai> findByDeletedAtIsNull(Pageable pageable);

    Optional<ChuongTrinhKhuyenMai> findByIdAndDeletedAtIsNull(Long id);
}
