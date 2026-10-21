package com.milktea.catalog.repository;

import com.milktea.catalog.entity.BienTheSanPham;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {
    java.util.Optional<BienTheSanPham> findByIdAndDeletedAtIsNull(Long id);

    List<BienTheSanPham> findAllBySanPhamIdAndDeletedAtIsNullOrderById(Long productId);

    @Query("select v.sanPham.id as productId, min(v.giaBan) as minPrice " +
            "from BienTheSanPham v where v.deletedAt is null and v.sanPham.id in :productIds " +
            "group by v.sanPham.id")
    List<ProductMinPrice> findMinimumPricesByProductIds(@Param("productIds") Collection<Long> productIds);

    boolean existsBySanPhamIdAndKichCoIgnoreCaseAndDeletedAtIsNull(Long productId, String size);

    boolean existsBySanPhamIdAndKichCoIgnoreCaseAndIdNotAndDeletedAtIsNull(Long productId, String size, Long id);
}
