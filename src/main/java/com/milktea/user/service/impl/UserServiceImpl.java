package com.milktea.user.service.impl;

import com.milktea.common.exception.BusinessException;
import com.milktea.user.dto.*;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.entity.VaiTro;
import com.milktea.user.repository.NguoiDungRepository;
import com.milktea.user.repository.VaiTroRepository;
import com.milktea.user.service.UserService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final NguoiDungRepository users;
    private final VaiTroRepository roles;
    private final PasswordEncoder encoder;

    public UserServiceImpl(NguoiDungRepository users, VaiTroRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Override
    public List<UserResponse> list(String role, String keyword) {
        List<NguoiDung> list = users.findAllActive();

        if (role != null && !role.isBlank()) {
            String cleanRole = role.trim().toUpperCase();
            list = list.stream()
                    .filter(u -> u.getVaiTro() != null && cleanRole.equalsIgnoreCase(u.getVaiTro().getTenVaiTro()))
                    .toList();
        }

        if (keyword != null && !keyword.isBlank()) {
            String cleanKeyword = keyword.trim().toLowerCase();
            list = list.stream()
                    .filter(u -> (u.getTenDangNhap() != null && u.getTenDangNhap().toLowerCase().contains(cleanKeyword))
                            || (u.getHoVaTen() != null && u.getHoVaTen().toLowerCase().contains(cleanKeyword))
                            || (u.getSoDienThoai() != null && u.getSoDienThoai().contains(cleanKeyword)))
                    .toList();
        }

        return list.stream().map(this::toResponse).toList();
    }

    @Override
    public UserResponse get(Long id) {
        NguoiDung u = users.findWithVaiTroById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng #" + id));
        if (u.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Tài khoản đã bị xóa #" + id);
        }
        return toResponse(u);
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String username = request.username().trim();
        if (users.existsByTenDangNhap(username)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Tên đăng nhập '" + username + "' đã tồn tại");
        }

        String roleName = request.role().trim().toUpperCase();
        VaiTro role = roles.findByTenVaiTro(roleName)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Vai trò không hợp lệ: " + roleName));

        NguoiDung u = new NguoiDung();
        u.setTenDangNhap(username);
        u.setMatKhauMaHoa(encoder.encode(request.password()));
        u.setHoVaTen(request.fullName().trim());
        u.setSoDienThoai(request.phone() != null ? request.phone().trim() : null);
        u.setVaiTro(role);
        u.setDangHoatDong(true);
        u.setNgayTao(Instant.now());

        NguoiDung saved = users.save(u);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        NguoiDung u = users.findWithVaiTroById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng #" + id));
        if (u.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Tài khoản đã bị xóa #" + id);
        }

        u.setHoVaTen(request.fullName().trim());
        u.setSoDienThoai(request.phone() != null ? request.phone().trim() : null);

        if (request.role() != null && !request.role().isBlank()) {
            String roleName = request.role().trim().toUpperCase();
            VaiTro role = roles.findByTenVaiTro(roleName)
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Vai trò không hợp lệ: " + roleName));
            u.setVaiTro(role);
        }

        if (request.isActive() != null) {
            u.setDangHoatDong(request.isActive());
        }

        if (request.password() != null && !request.password().isBlank()) {
            u.setMatKhauMaHoa(encoder.encode(request.password().trim()));
        }

        NguoiDung saved = users.save(u);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, boolean isActive) {
        NguoiDung u = users.findWithVaiTroById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng #" + id));
        u.setDangHoatDong(isActive);
        return toResponse(users.save(u));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NguoiDung u = users.findWithVaiTroById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng #" + id));
        if (id == 1 || "admin".equalsIgnoreCase(u.getTenDangNhap())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Không thể xóa tài khoản Quản trị viên hệ thống (Admin)");
        }
        u.markDeleted();
        u.setDangHoatDong(false);
        users.save(u);
    }

    @Override
    public List<RoleResponse> listRoles() {
        return roles.findAll().stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(r -> new RoleResponse(r.getId(), r.getTenVaiTro(), r.getMoTa()))
                .toList();
    }

    private UserResponse toResponse(NguoiDung u) {
        String roleName = u.getVaiTro() != null ? u.getVaiTro().getTenVaiTro() : "CASHIER";
        Long roleId = u.getVaiTro() != null ? u.getVaiTro().getId() : 0L;
        return new UserResponse(
                u.getId(),
                u.getTenDangNhap(),
                u.getHoVaTen(),
                u.getSoDienThoai(),
                roleName,
                roleId,
                u.isDangHoatDong(),
                u.getNgayTao() != null ? u.getNgayTao() : u.getCreatedAt()
        );
    }
}