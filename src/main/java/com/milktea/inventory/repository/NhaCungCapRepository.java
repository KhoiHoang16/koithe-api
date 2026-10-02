package com.milktea.inventory.repository;

import com.milktea.inventory.entity.NhaCungCap;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhaCungCapRepository extends JpaRepository<NhaCungCap, Long> {
    Optional<NhaCungCap> findByIdAndDeletedAtIsNull(Long id);

    Page<NhaCungCap> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsBySoDienThoaiAndDeletedAtIsNull(String soDienThoai);

    boolean existsBySoDienThoaiAndIdNotAndDeletedAtIsNull(String soDienThoai, Long id);
}
