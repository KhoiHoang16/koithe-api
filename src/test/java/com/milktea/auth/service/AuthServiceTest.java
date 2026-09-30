package com.milktea.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.milktea.auth.dto.LoginRequest;
import com.milktea.auth.dto.AuthResponse;
import com.milktea.auth.service.impl.AuthServiceImpl;
import com.milktea.common.exception.BusinessException;
import com.milktea.security.JwtService;
import com.milktea.user.entity.NguoiDung;
import com.milktea.user.entity.VaiTro;
import com.milktea.user.repository.NguoiDungRepository;
import com.milktea.user.repository.VaiTroRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock NguoiDungRepository users;
    @Mock VaiTroRepository roles;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> values;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(users, roles, encoder, jwt, redis, 7);
    }

    @Test
    void loginReturnsTokensAndStoresRefreshTokenInRedis() {
        VaiTro role = new VaiTro(); role.setTenVaiTro("ADMIN");
        NguoiDung user = new NguoiDung(); user.setId(1L); user.setTenDangNhap("admin");
        user.setMatKhauMaHoa("encoded"); user.setHoVaTen("Admin"); user.setDangHoatDong(true); user.setVaiTro(role);
        when(users.findByTenDangNhap("admin")).thenReturn(Optional.of(user));
        when(encoder.matches("Admin@123", "encoded")).thenReturn(true);
        when(jwt.refreshToken(any())).thenReturn("refresh-jwt");
        when(jwt.accessToken(any())).thenReturn("access-jwt");
        when(redis.opsForValue()).thenReturn(values);

        AuthResponse response = service.login(new LoginRequest("admin", "Admin@123"));

        assertEquals("access-jwt", response.accessToken());
        assertEquals("refresh-jwt", response.refreshToken());
        verify(values).set("auth:refresh:refresh-jwt", "admin", java.time.Duration.ofDays(7));
    }

    @Test
    void loginRejectsIncorrectPassword() {
        NguoiDung user = new NguoiDung(); user.setMatKhauMaHoa("encoded"); user.setDangHoatDong(true);
        when(users.findByTenDangNhap("admin")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.login(new LoginRequest("admin", "wrong")));
        verifyNoInteractions(jwt, redis);
    }
}
