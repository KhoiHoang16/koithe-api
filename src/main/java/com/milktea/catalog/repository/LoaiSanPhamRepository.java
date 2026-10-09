package com.milktea.catalog.repository;

import com.milktea.catalog.entity.LoaiSanPham;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoaiSanPhamRepository extends JpaRepository<LoaiSanPham, Long> {
    List<LoaiSanPham> findAllByDeletedAtIsNullOrderByTenDanhMucAsc();

    boolean existsByTenDanhMucIgnoreCaseAndDeletedAtIsNull(String tenDanhMuc);
}
