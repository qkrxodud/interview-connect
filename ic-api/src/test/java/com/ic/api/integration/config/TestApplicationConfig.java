package com.ic.api.integration.config;

import com.ic.api.config.SecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

/**
 * 통합 테스트용 애플리케이션 설정
 * - 테스트 프로필에서만 활성화
 * - Redis/JPA 자동 구성 비활성화는 application-test.yml에서 처리
 */
@TestConfiguration
@Profile("test")
@ComponentScan(
    basePackages = {
        "com.ic.api.auth",
        "com.ic.api.review",
        "com.ic.api.company",
        "com.ic.api.qa",
        "com.ic.api.config.security"
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
public class TestApplicationConfig {
}
