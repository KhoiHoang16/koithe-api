package com.milktea.user.repository;

import com.milktea.user.entity.NguoiDung;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    @EntityGraph(attributePaths = "vaiTro")
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);

    @EntityGraph(attributePaths = "vaiTro")
    Optional<NguoiDung> findWithVaiTroById(Long id);

    boolean existsByTenDangNhap(String tenDangNhap);
}
