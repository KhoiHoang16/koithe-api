package com.milktea.auth.controller; 
import com.milktea.auth.dto.*; 
import com.milktea.auth.service.AuthService; 
import com.milktea.common.response.ApiResponse; 
import com.milktea.security.UserPrincipal; 
import jakarta.validation.Valid; 
import org.springframework.http.*; 
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.*;
@RestController 
@RequestMapping("/api/auth") 
public class AuthController { 
    private final AuthService service; 
    public AuthController(AuthService service){this.service=service;} 
    @PostMapping("/login") 
    public ApiResponse<AuthResponse> 
    login(@Valid @RequestBody LoginRequest r){return ApiResponse.success(service.login(r));} 
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) 
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest r){return ApiResponse.success(service.register(r));} 
    @PostMapping("/refresh") public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest r){return ApiResponse.success(service.refresh(r));} 
    @PostMapping("/logout") 
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void logout(@RequestBody(required=false) RefreshRequest r){service.logout(r==null?null:r.refreshToken());} 
    @GetMapping("/me") public ApiResponse<MeResponse> me(@AuthenticationPrincipal UserPrincipal p){return ApiResponse.success(service.me(p));} }
