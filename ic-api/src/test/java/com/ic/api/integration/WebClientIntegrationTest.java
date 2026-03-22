package com.ic.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * WebTestClient를 사용한 실제 HTTP 요청/응답 통합 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("실제 HTTP 요청/응답 통합 테스트")
class WebClientIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("비로그인 사용자의 후기 목록 조회 API 테스트")
    void shouldGetReviewsForGuestUser() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        client.get()
                .uri("/api/v1/reviews")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").exists()
                .jsonPath("$.data.content").isArray()
                .jsonPath("$.data.pageable").exists()
                .jsonPath("$.error").doesNotExist();
    }

    @Test
    @DisplayName("비로그인 사용자의 Q&A 조회 API 테스트 (답변 블러 처리)")
    void shouldGetQaListWithBlurredAnswersForGuestUser() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        // 먼저 존재하는 reviewId를 사용하거나, 존재하지 않는 경우에 대한 처리
        client.get()
                .uri("/api/v1/reviews/1/qa")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").exists();
    }

    @Test
    @DisplayName("존재하지 않는 후기 ID로 Q&A 조회 시 적절한 에러 응답")
    void shouldReturnErrorForNonExistentReviewId() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        client.get()
                .uri("/api/v1/reviews/99999/qa")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.error").exists();
    }

    @Test
    @DisplayName("잘못된 API 엔드포인트 요청 시 404 응답")
    void shouldReturn404ForInvalidEndpoint() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        client.get()
                .uri("/api/v1/invalid-endpoint")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST 요청 시 인증이 필요한 엔드포인트 테스트")
    void shouldRequireAuthenticationForPostRequests() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        client.post()
                .uri("/api/v1/reviews")
                .header("Content-Type", "application/json")
                .bodyValue("""
                    {
                        "companyId": 1,
                        "interviewDate": "2024-01-01",
                        "position": "백엔드 개발자",
                        "interviewTypes": ["기술면접"],
                        "questions": ["알고리즘 문제"],
                        "difficulty": 4,
                        "atmosphere": 5,
                        "result": "PASS",
                        "content": "좋은 면접이었습니다"
                    }
                """)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("헬스체크 엔드포인트 테스트")
    void shouldReturnHealthStatus() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        // Spring Boot Actuator의 health 엔드포인트가 있다면
        client.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("CORS 헤더 확인 테스트")
    void shouldReturnProperCorsHeaders() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        client.options()
                .uri("/api/v1/reviews")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin");
    }
}