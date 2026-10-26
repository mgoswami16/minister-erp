package com.ministererp.service;

import com.ministererp.dto.request.LoginRequest;
import com.ministererp.dto.response.LoginResponse;
import com.ministererp.model.User;
import com.ministererp.security.JwtTokenProvider;
import com.ministererp.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager
            authenticationManager;
    private final JwtTokenProvider
            jwtTokenProvider;
    private final GoogleSheetsService
            googleSheetsService;
    private final AuditService auditService;
    private final DateUtil dateUtil;

    // ─────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────
    public LoginResponse login(
            LoginRequest request) {

        // Step 1: Authenticate
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        // Step 2: Get user from sheets
        User user = googleSheetsService
                .findUserByUsername(
                        request.getUsername());

        if (user == null) {
            throw new RuntimeException(
                    "User not found");
        }

        // Step 3: Generate JWT token
        String token = jwtTokenProvider
                .generateTokenWithClaims(
                        user.getUsername(),
                        user.getRole(),
                        user.getUserId(),
                        user.getName()
                );

        // Step 4: Update last login
        user.setLastLogin(dateUtil.now());
        googleSheetsService.updateUser(user);

        // Step 5: Log login activity
        auditService.logAction(
                user.getUserId(),
                user.getName(),
                user.getRole(),
                "USER_LOGIN",
                null,
                null,
                null,
                "User logged in successfully",
                null
        );

        // Step 6: Return response
        return LoginResponse.of(
                token,
                jwtTokenProvider
                        .getExpirationTime(),
                user.getUserId(),
                user.getName(),
                user.getRole(),
                user.getDepartment(),
                user.getUsername(),
                user.getMobile(),
                user.getEmail()
        );
    }

    // ─────────────────────────────────────────
    // VALIDATE TOKEN
    // ─────────────────────────────────────────
    public boolean validateToken(String token) {
        return jwtTokenProvider
                .isValidToken(token);
    }

    // ─────────────────────────────────────────
    // GET CURRENT USER FROM TOKEN
    // ─────────────────────────────────────────
    public User getCurrentUser(String token) {
        String username = jwtTokenProvider
                .extractUsername(token);
        return googleSheetsService
                .findUserByUsername(username);
    }

    // ─────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────
    public void logout(
            String userId,
            String userName,
            String role) {
        auditService.logAction(
                userId,
                userName,
                role,
                "USER_LOGOUT",
                null,
                null,
                null,
                "User logged out",
                null
        );
    }
}