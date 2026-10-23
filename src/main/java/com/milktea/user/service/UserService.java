package com.milktea.user.service;

import com.milktea.user.dto.*;
import java.util.List;

public interface UserService {
    List<UserResponse> list(String role, String keyword);
    UserResponse get(Long id);
    UserResponse create(UserCreateRequest request);
    UserResponse update(Long id, UserUpdateRequest request);
    UserResponse updateStatus(Long id, boolean isActive);
    void delete(Long id);
    List<RoleResponse> listRoles();
}
