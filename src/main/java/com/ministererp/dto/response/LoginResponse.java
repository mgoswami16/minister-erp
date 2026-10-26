package com.ministererp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private String userId;
    private String name;
    private String role;
    private String department;
    private String username;
    private String mobile;
    private String email;

    // ── Build standard response
    public static LoginResponse of(
            String token,
            long expiresIn,
            String userId,
            String name,
            String role,
            String department,
            String username,
            String mobile,
            String email) {

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(userId)
                .name(name)
                .role(role)
                .department(department)
                .username(username)
                .mobile(mobile)
                .email(email)
                .build();
    }
}