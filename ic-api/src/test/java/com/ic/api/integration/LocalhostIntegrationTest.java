package com.ic.api.integration;

import com.ic.api.integration.config.IntegrationTestFakesConfig;
import com.ic.api.integration.config.TestApplicationConfig;
import com.ic.api.integration.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * localhost HTTP 요청 통합 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({IntegrationTestFakesConfig.class, TestApplicationConfig.class, TestSecurityConfig.class})
@DisplayName("localhost HTTP 요청 통합 테스트")
class LocalhostIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @DisplayName("서버가 정상적으로 시작되고 후기 목록을 조회할 수 있다")
    void shouldGetReviewsSuccessfully() {
        String url = "http://localhost:" + port + "/api/v1/reviews";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트는 404를 반환한다")
    void shouldReturn404ForNonExistentEndpoint() {
        String url = "http://localhost:" + port + "/api/v1/nonexistent";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("특정 후기의 Q&A를 조회할 수 있다")
    void shouldGetQaForReview() {
        String url = "http://localhost:" + port + "/api/v1/reviews/1/qa";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // 후기가 존재하지 않아도 적절한 응답이 와야 함 (빈 리스트 또는 404)
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode() == HttpStatus.NOT_FOUND).isTrue();
    }

    @Test
    @DisplayName("인증 없이 POST 요청 시 401을 반환한다")
    void shouldReturn401ForUnauthenticatedPostRequest() {
        String url = "http://localhost:" + port + "/api/v1/reviews";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("회사 목록을 조회할 수 있다")
    void shouldGetCompaniesSuccessfully() {
        String url = "http://localhost:" + port + "/api/v1/companies";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("특정 후기를 조회할 수 있다")
    void shouldGetReviewById() {
        String url = "http://localhost:" + port + "/api/v1/reviews/1";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // 후기가 존재하지 않을 수 있으므로 200 또는 404 둘 다 허용
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode() == HttpStatus.NOT_FOUND).isTrue();
    }
}