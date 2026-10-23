package com.milktea.user.repository;

import com.milktea.user.entity.NguoiDung;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    @EntityGraph(attributePaths = "vaiTro")
    Optional<NguoiDung> findByTenDangNhap(String tenDangNhap);

    @EntityGraph(attributePaths = "vaiTro")
    Optional<NguoiDung> findWithVaiTroById(Long id);

    @EntityGraph(attributePaths = "vaiTro")
    @Query("SELECT u FROM NguoiDung u WHERE u.deletedAt IS NULL ORDER BY u.id ASC")
    List<NguoiDung> findAllActive();

    boolean existsByTenDangNhap(String tenDangNhap);
}