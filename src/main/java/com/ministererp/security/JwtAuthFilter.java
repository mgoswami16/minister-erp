package com.ministererp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    // ─────────────────────────────────────────
    // MAIN FILTER METHOD
    // ─────────────────────────────────────────
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1: Extract JWT token from request header
            String jwt = extractJwtFromRequest(request);

            // Step 2: Validate token and set authentication
            if (StringUtils.hasText(jwt)
                    && jwtTokenProvider.isValidToken(jwt)) {

                // Step 3: Get username from token
                String username =
                        jwtTokenProvider.extractUsername(jwt);

                // Step 4: Load user details from database/sheets
                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                // Step 5: Validate token against user details
                if (jwtTokenProvider.validateToken(
                        jwt, userDetails)) {

                    // Step 6: Create authentication object
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Step 7: Set request details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    // Step 8: Set authentication in Security Context
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (Exception ex) {
            // Log error but don't stop the filter chain
            System.out.println(
                    "JWT Authentication Error: "
                    + ex.getMessage());
        }

        // Step 9: Continue filter chain
        filterChain.doFilter(request, response);
    }

    // ─────────────────────────────────────────
    // EXTRACT JWT FROM REQUEST HEADER
    // ─────────────────────────────────────────
    private String extractJwtFromRequest(
            HttpServletRequest request) {

        // Get Authorization header
        String bearerToken =
                request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith("Bearer ")) {

            // Return token without "Bearer " prefix
            return bearerToken.substring(7);
        }

        return null;
    }

    // ─────────────────────────────────────────
    // SKIP FILTER FOR PUBLIC URLS
    // ─────────────────────────────────────────
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request)
            throws ServletException {

        String path = request.getRequestURI();

        // Skip JWT filter for these public endpoints
        return path.contains("/auth/login")
                || path.contains("/auth/refresh")
                || path.contains("/health")
                || path.contains("/actuator")
                || path.contains("/error");
    }
}