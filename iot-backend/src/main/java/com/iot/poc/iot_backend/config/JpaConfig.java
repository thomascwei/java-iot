package com.iot.poc.iot_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider") // 指定使用自定義的供應器
public class JpaConfig {

    @Bean
    public DateTimeProvider dateTimeProvider() {
        // 強制讓 Auditing 功能產生 OffsetDateTime
        return () -> Optional.of(OffsetDateTime.now());
    }
}