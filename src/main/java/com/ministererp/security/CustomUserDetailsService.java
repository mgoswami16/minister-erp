package com.ministererp.security;

import com.ministererp.model.User;
import com.ministererp.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final GoogleSheetsService googleSheetsService;

    // ─────────────────────────────────────────
    // LOAD USER BY USERNAME
    // Called by Spring Security during login
    // ─────────────────────────────────────────
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Find user from Google Sheets
        User user = googleSheetsService
                .findUserByUsername(username);

        // If user not found throw exception
        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found with username: "
                    + username);
        }

        // If user is disabled
        if (!user.isActive()) {
            throw new UsernameNotFoundException(
                    "User account is disabled: "
                    + username);
        }

        // Build Spring Security UserDetails
        return buildUserDetails(user);
    }

    // ─────────────────────────────────────────
    // LOAD USER BY USER ID
    // ─────────────────────────────────────────
    public UserDetails loadUserByUserId(String userId)
            throws UsernameNotFoundException {

        User user = googleSheetsService
                .findUserById(userId);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found with ID: " + userId);
        }

        return buildUserDetails(user);
    }

    // ─────────────────────────────────────────
    // BUILD SPRING SECURITY USER DETAILS
    // ─────────────────────────────────────────
    private UserDetails buildUserDetails(User user) {

        // Create role authority with ROLE_ prefix
        // Example: MINISTER → ROLE_MINISTER
        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole()
                        )
                );

        // Return Spring Security User object
        return org.springframework.security.core.userdetails
                .User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}