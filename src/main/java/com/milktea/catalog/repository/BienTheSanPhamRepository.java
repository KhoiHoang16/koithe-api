package com.milktea.catalog.repository;
import com.milktea.catalog.entity.BienTheSanPham;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {
    java.util.Optional<BienTheSanPham> findByIdAndDeletedAtIsNull(Long id);
    List<BienTheSanPham> findAllBySanPhamIdAndDeletedAtIsNullOrderById(Long productId);
    boolean existsBySanPhamIdAndKichCoIgnoreCaseAndDeletedAtIsNull(Long productId, String size);
    boolean existsBySanPhamIdAndKichCoIgnoreCaseAndIdNotAndDeletedAtIsNull(Long productId, String size, Long id);
}
