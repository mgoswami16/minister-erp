package com.ministererp.controller;

import com.ministererp.dto.request.LoginRequest;
import com.ministererp.dto.response.ApiResponse;
import com.ministererp.dto.response.LoginResponse;
import com.ministererp.model.User;
import com.ministererp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────────────────────
    // LOGIN
    // POST /api/v1/auth/login
    // ─────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>>
    login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Login successful. Welcome "
                        + response.getName()));
    }

    // ─────────────────────────────────────────
    // VALIDATE TOKEN
    // GET /api/v1/auth/validate
    // ─────────────────────────────────────────
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>>
    validate(
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader
                .replace("Bearer ", "");

        boolean isValid =
                authService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Token is valid",
                            "VALID"));
        } else {
            return ResponseEntity.ok(
                    ApiResponse.<String>error(
                            "Token is invalid "
                            + "or expired",
                            "INVALID_TOKEN"));
        }
    }

    // ─────────────────────────────────────────
    // GET CURRENT USER
    // GET /api/v1/auth/me
    // ─────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>>
    getCurrentUser(
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader
                .replace("Bearer ", "");

        User user =
                authService.getCurrentUser(token);

        return ResponseEntity.ok(
                ApiResponse.success(
                        user,
                        "User details fetched"));
    }

    // ─────────────────────────────────────────
    // LOGOUT
    // POST /api/v1/auth/logout
    // ─────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>>
    logout(
            @RequestHeader("Authorization")
            String authHeader) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logged out successfully"));
    }

    // ─────────────────────────────────────────
    // HEALTH CHECK
    // GET /api/v1/auth/health
    // ─────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>>
    health() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Minister ERP API "
                        + "is running ✅",
                        "HEALTHY"));
    }
}