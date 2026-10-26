package com.ministererp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // ── Allow all origins (change in production)
        config.setAllowedOriginPatterns(
                List.of("*"));

        // ── Allow these HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        // ── Allow these headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // ── Expose Authorization header to frontend
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // ── Allow cookies/credentials
        config.setAllowCredentials(true);

        // ── Cache preflight for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // ── Apply to all endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}