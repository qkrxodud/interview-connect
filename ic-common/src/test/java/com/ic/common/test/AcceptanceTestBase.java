package com.ic.common.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * API 엔드포인트 인수 테스트를 위한 베이스 클래스
 * - Spring Boot 전체 컨텍스트 (~8s)
 * - WebTestClient를 통한 HTTP 요청/응답 검증
 * - 실제 사용자 시나리오 검증
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AcceptanceTestBase {

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;

    @BeforeEach
    void acceptanceTestSetUp() {
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }
}