package com.milktea.promotion.repository;

import com.milktea.promotion.entity.KhuyenMaiSanPham;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhuyenMaiSanPhamRepository extends JpaRepository<KhuyenMaiSanPham, Long> {
    Page<KhuyenMaiSanPham> findByDeletedAtIsNull(Pageable pageable);

    Optional<KhuyenMaiSanPham> findByIdAndDeletedAtIsNull(Long id);

    List<KhuyenMaiSanPham> findByChuongTrinhIdAndDeletedAtIsNull(Long chuongTrinhId);

    boolean existsByChuongTrinhIdAndDeletedAtIsNull(Long chuongTrinhId);
}
