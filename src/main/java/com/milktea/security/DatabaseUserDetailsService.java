package com.milktea.security;

import com.milktea.user.repository.NguoiDungRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final NguoiDungRepository users;

    public DatabaseUserDetailsService(NguoiDungRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return users.findByTenDangNhap(username)
            .map(UserPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
    }
}
