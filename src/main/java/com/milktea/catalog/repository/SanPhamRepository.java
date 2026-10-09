package com.milktea.catalog.repository;

import com.milktea.catalog.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SanPhamRepository extends JpaRepository<SanPham, Long>, JpaSpecificationExecutor<SanPham> {
    boolean existsByDanhMucIdAndDeletedAtIsNull(Long categoryId);
}
