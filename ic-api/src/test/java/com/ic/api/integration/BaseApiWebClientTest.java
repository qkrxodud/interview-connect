package com.ic.api.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ic.api.integration.config.IntegrationTestFakesConfig;
import com.ic.api.integration.config.TestApplicationConfig;
import com.ic.api.integration.config.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient를 사용한 API 통합 테스트 기반 클래스
 * - 실제 HTTP 요청/응답 테스트
 * - Spring Boot 컨텍스트 공유로 빠른 실행
 * - Fake 구현체로 외부 의존성 제거
 * - 트랜잭션 롤백으로 테스트 격리
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({IntegrationTestFakesConfig.class, TestApplicationConfig.class, TestSecurityConfig.class})
@Transactional
public abstract class BaseApiWebClientTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected IntegrationTestFakesConfig fakesConfig;

    protected WebClient webClient;
    protected String baseUrl;

    @BeforeEach
    void setUpWebClient() {
        baseUrl = "http://localhost:" + port;
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        // 테스트 시작 전 상태 초기화 (이전 테스트 데이터 격리)
        fakesConfig.resetAllFakes();
    }

    @AfterEach
    void cleanUpFakes() {
        // 모든 Fake 구현체 초기화
        fakesConfig.resetAllFakes();
    }

    /**
     * 회원가입 후 이메일 인증까지 완료하는 HTTP 헬퍼
     * - 이메일 인증이 필요한 로그인 테스트에서 사용
     */
    protected void signupAndVerifyViaHttp(final String email, final String password, final String nickname) {
        final String signupBody = toJson(java.util.Map.of(
                "email", email,
                "password", password,
                "passwordConfirm", password,
                "nickname", nickname
        ));
        webClient.post()
                .uri("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signupBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        final String code = fakesConfig.getEmailService().getVerificationCode(email);
        final String verifyBody = toJson(java.util.Map.of("email", email, "code", code));
        webClient.post()
                .uri("/api/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(verifyBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 인증된 요청을 위한 헤더 생성
     */
    protected HttpHeaders createAuthHeaders(String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    /**
     * JSON 요청 본문을 문자열로 변환
     */
    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    /**
     * JSON 응답을 객체로 변환
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    /**
     * JSON 응답을 제네릭 타입으로 변환 (TypeReference 사용)
     */
    protected <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    /**
     * 기본 테스트 회원 생성 헬퍼
     */
    protected TestMember createTestMember(String email, String nickname) {
        return new TestMember(email, "password123", nickname);
    }

    /**
     * 테스트용 회원 정보 래퍼
     */
    public static class TestMember {
        public final String email;
        public final String password;
        public final String nickname;

        public TestMember(String email, String password, String nickname) {
            this.email = email;
            this.password = password;
            this.nickname = nickname;
        }
    }
}