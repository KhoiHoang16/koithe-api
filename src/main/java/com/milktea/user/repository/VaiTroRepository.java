package com.milktea.user.repository;

import com.milktea.user.entity.VaiTro;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    Optional<VaiTro> findByTenVaiTro(String tenVaiTro);
}
