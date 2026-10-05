package com.milktea.inventory.repository;

import com.milktea.inventory.entity.ChiTietPhieuNhap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietPhieuNhapRepository extends JpaRepository<ChiTietPhieuNhap, Long> {
    Optional<ChiTietPhieuNhap> findByIdAndDeletedAtIsNull(Long id);

    List<ChiTietPhieuNhap> findAllByPhieuNhapHangIdAndDeletedAtIsNull(Long phieuNhapHangId);

    Page<ChiTietPhieuNhap> findAllByPhieuNhapHangIdAndDeletedAtIsNull(Long phieuNhapHangId, Pageable pageable);
}
