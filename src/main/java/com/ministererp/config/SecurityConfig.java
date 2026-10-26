package com.ministererp.config;

import com.ministererp.security.JwtAuthFilter;
import com.ministererp.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    // ── Public URLs - No JWT needed
    private static final String[] PUBLIC_URLS = {
            "/auth/login",
            "/auth/refresh",
            "/health",
            "/actuator/**",
            "/error"
    };

    // ─────────────────────────────────────────
    // SECURITY FILTER CHAIN
    // ─────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            // Disable CSRF - using JWT
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session - no server sessions
            .sessionManagement(session -> session
                    .sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))

            // URL Authorization Rules
            .authorizeHttpRequests(auth -> auth

                // Public endpoints
                .requestMatchers(PUBLIC_URLS)
                    .permitAll()

                // Minister only endpoints
                .requestMatchers(
                        "/minister/**",
                        "/users/**",
                        "/analytics/**")
                    .hasRole("MINISTER")

                // Operator endpoints
                .requestMatchers("/operator/**")
                    .hasAnyRole("OPERATOR", "MINISTER")

                // Officer endpoints
                .requestMatchers("/officer/**")
                    .hasAnyRole("OFFICER", "MINISTER")

                // Shared authenticated endpoints
                .requestMatchers(
                        "/complaints/**",
                        "/appointments/**",
                        "/reports/**",
                        "/notifications/**",
                        "/dashboard/**")
                    .authenticated()

                // All other requests need authentication
                .anyRequest().authenticated()
            )

            // Set authentication provider
            .authenticationProvider(
                    authenticationProvider())

            // Add JWT filter before Spring Security filter
            .addFilterBefore(
                    jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─────────────────────────────────────────
    // AUTHENTICATION PROVIDER
    // ─────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // ─────────────────────────────────────────
    // AUTHENTICATION MANAGER
    // ─────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}