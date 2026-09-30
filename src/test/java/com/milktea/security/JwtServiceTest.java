package com.milktea.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private final JwtService jwt = new JwtService("0123456789abcdef0123456789abcdef", 15, 7);

    @Test
    void createsAccessAndRefreshTokensWithExpectedClaims() {
        UserPrincipal principal = new UserPrincipal(1L, "admin", "hash", true, "ADMIN");

        String accessToken = jwt.accessToken(principal);
        String refreshToken = jwt.refreshToken(principal);
        Claims access = jwt.claims(accessToken);
        Claims refresh = jwt.claims(refreshToken);

        assertEquals("admin", access.getSubject());
        assertEquals("ADMIN", access.get("role"));
        assertEquals("access", access.get("type"));
        assertFalse(jwt.isRefresh(accessToken));
        assertTrue(jwt.isRefresh(refreshToken));
        assertTrue(refresh.getExpiration().after(access.getExpiration()));
    }
}
