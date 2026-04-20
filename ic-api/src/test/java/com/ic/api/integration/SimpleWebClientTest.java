package com.ic.api.integration;

import com.ic.api.integration.config.IntegrationTestFakesConfig;
import com.ic.api.integration.config.TestApplicationConfig;
import com.ic.api.integration.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * 간단한 WebTestClient 통합 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({IntegrationTestFakesConfig.class, TestApplicationConfig.class, TestSecurityConfig.class})
@DisplayName("간단한 WebTestClient 통합 테스트")
class SimpleWebClientTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("서버가 정상적으로 시작되고 응답한다")
    void shouldServerStartAndRespond() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        client.get()
                .uri("/api/v1/reviews")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json");
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트는 404를 반환한다")
    void shouldReturn404ForNonExistentEndpoint() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        // GET /api/v1/** 는 permitAll이므로 DispatcherServlet까지 도달하여 404 반환
        client.get()
                .uri("/api/v1/nonexistent")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Q&A 엔드포인트가 응답한다")
    void shouldQaEndpointRespond() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        // 임의의 reviewId로 테스트 (존재하지 않으면 404, 존재하면 200)
        client.get()
                .uri("/api/v1/reviews/1/qa")
                .exchange()
                .expectStatus().value(status ->
                        org.assertj.core.api.Assertions.assertThat(status).isBetween(200, 499))
                .expectHeader().contentType("application/json");
    }

    @Test
    @DisplayName("POST 요청 없이 인증이 필요한지 확인")
    void shouldCheckAuthenticationRequired() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        client.post()
                .uri("/api/v1/reviews")
                .header("Content-Type", "application/json")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}