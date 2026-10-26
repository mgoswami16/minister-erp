package com.ministererp.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executor;

@Configuration
@EnableCaching
@EnableAsync
public class AppConfig {

    // ─────────────────────────────────────────
    // PASSWORD ENCODER
    // BCrypt with strength 12
    // ─────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ─────────────────────────────────────────
    // WEB CLIENT
    // For calling Google Sheets API
    // ─────────────────────────────────────────
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer ->
                        configurer
                                .defaultCodecs()
                                .maxInMemorySize(
                                        10 * 1024 * 1024)) // 10MB
                .build();
    }

    // ─────────────────────────────────────────
    // ASYNC EXECUTOR
    // For background tasks like notifications
    // ─────────────────────────────────────────
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(
                "MinisterERP-Async-");
        executor.initialize();

        return executor;
    }
}