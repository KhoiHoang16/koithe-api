package com.milktea.config;
import com.milktea.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                // 1. Các endpoint công khai hệ thống, xác thực auth, giỏ hàng, tài liệu Swagger
                .requestMatchers(
                    "/api/health", "/api/auth/login", "/api/auth/register",
                    "/api/auth/refresh", "/api/auth/logout", "/api/carts/**",
                    "/actuator/health/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
                ).permitAll()

                // 2. Callback & Webhook cổng thanh toán (VNPay & MoMo):
                // BẮT BUỘC mở permitAll vì máy chủ đối tác (MoMo/VNPay) và trình duyệt redirect
                // không mang theo JWT Token của người dùng.
                // Việc bảo mật chống giả mạo được đảm bảo 100% bằng Chữ ký số bí mật (Signature/Checksum).
                .requestMatchers(
                    "/api/payments/vnpay-return", "/api/payments/vnpay-ipn",
                    "/api/payments/momo-return", "/api/payments/momo-ipn",
                    "/api/payments/vietqr-webhook",
                    "/api/payments/dev-simulate-success"
                ).permitAll()

                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**", "/api/products/**", "/api/toppings/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint((request, response, exception) -> response.sendError(401)))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
