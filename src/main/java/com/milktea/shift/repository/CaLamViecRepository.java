package com.milktea.shift.repository;

import com.milktea.shift.entity.CaLamViec;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaLamViecRepository extends JpaRepository<CaLamViec, Long> {
    Optional<CaLamViec> findByIdAndDeletedAtIsNull(Long id);

    List<CaLamViec> findAllByDeletedAtIsNullOrderByThoiGianBatDauDesc();

    boolean existsByThuNganIdAndTrangThaiAndDeletedAtIsNull(Long thuNganId, String trangThai);
}
