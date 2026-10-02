package com.milktea.table.repository;

import com.milktea.table.entity.Ban;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanRepository extends JpaRepository<Ban, Long> {
    Optional<Ban> findByIdAndDeletedAtIsNull(Long id);

    List<Ban> findAllByDeletedAtIsNullOrderBySoBanAsc();

    boolean existsBySoBanAndDeletedAtIsNull(String soBan);

    boolean existsBySoBanAndIdNotAndDeletedAtIsNull(String soBan, Long id);
}
