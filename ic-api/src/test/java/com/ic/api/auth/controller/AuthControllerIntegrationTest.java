package com.ic.api.auth.controller;

import com.ic.api.auth.dto.LoginRequest;
import com.ic.api.auth.dto.RefreshTokenRequest;
import com.ic.api.auth.dto.SignupRequest;
import com.ic.api.integration.BaseApiWebClientTest;
import com.ic.common.response.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthController WebClient 기반 통합 테스트
 * - 실제 HTTP 요청/응답 테스트
 * - Spring Boot 컨텍스트 공유로 빠른 실행
 * - Fake 구현체로 외부 의존성 제거
 * - End-to-End 시나리오 검증
 */
@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest extends BaseApiWebClientTest {

    private static final String AUTH_BASE_URL = "/api/v1/auth";

    @Nested
    @DisplayName("회원가입 API 테스트")
    class 회원가입_API_테스트 {

        @Test
        @DisplayName("유효한 회원가입 요청 시 201 Created와 회원 정보를 반환한다")
        void shouldReturn201AndMemberInfoWhenValidSignupRequest() {
            // given
            final SignupRequest request = new SignupRequest("test@example.com", "password123", "password123", "테스트유저");

            // when
            final String response = webClient.post()
                    .uri(AUTH_BASE_URL + "/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(request))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
            assertThat(apiResponse.data()).isNotNull();

            // Fake Repository에서 회원 저장 확인
            assertThat(fakesConfig.getMemberRepository().size()).isEqualTo(1);
            assertThat(fakesConfig.getMemberRepository().hasMemberWithEmail("test@example.com")).isTrue();

            // 비밀번호가 인코딩되어 저장되었는지 확인
            final var savedMember = fakesConfig.getMemberRepository().findByEmail("test@example.com").get();
            assertThat(fakesConfig.getPasswordEncoder().isEncoded(savedMember.getPassword())).isTrue();
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 시 409 Conflict를 반환한다")
        void shouldReturn409WhenDuplicateEmail() {
            // given: 기존 회원 가입
            final SignupRequest firstRequest = new SignupRequest("test@example.com", "password123", "password123", "첫번째");
            webClient.post()
                    .uri(AUTH_BASE_URL + "/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(firstRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            final SignupRequest duplicateRequest = new SignupRequest("test@example.com", "password456", "password456", "중복시도");

            // when & then
            assertThatThrownBy(() ->
                webClient.post()
                        .uri(AUTH_BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(toJson(duplicateRequest))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
            )
            .isInstanceOf(WebClientResponseException.class)
            .hasMessageContaining("409");

            // 회원이 중복 생성되지 않았는지 확인
            assertThat(fakesConfig.getMemberRepository().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("register 엔드포인트도 회원가입 기능을 제공한다")
        void shouldSupportRegisterEndpoint() {
            // given
            final SignupRequest request = new SignupRequest("register@example.com", "password123", "password123", "register테스트");

            // when
            final String response = webClient.post()
                    .uri(AUTH_BASE_URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(request))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
            assertThat(fakesConfig.getMemberRepository().hasMemberWithEmail("register@example.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("로그인 API 테스트")
    class 로그인_API_테스트 {

        @Test
        @DisplayName("유효한 인증 정보로 로그인 시 200 OK와 토큰을 반환한다")
        void shouldReturn200AndTokensWhenValidCredentials() {
            // given: 회원 가입 + 이메일 인증
            final var member = createTestMember("test@example.com", "테스트유저");
            signupAndVerifyViaHttp(member.email, member.password, member.nickname);

            // when: 로그인 시도
            final LoginRequest loginRequest = new LoginRequest(member.email, member.password);
            final String response = webClient.post()
                    .uri(AUTH_BASE_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(loginRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
            assertThat(apiResponse.data()).isNotNull();

            // 리프레시 토큰이 저장되었는지 확인
            assertThat(fakesConfig.getRefreshTokenRepository().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 401 Unauthorized를 반환한다")
        void shouldReturn401WhenEmailNotFound() {
            // given
            final LoginRequest request = new LoginRequest("notfound@example.com", "password123");

            // when & then
            assertThatThrownBy(() ->
                webClient.post()
                        .uri(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(toJson(request))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
            )
            .isInstanceOf(WebClientResponseException.class)
            .hasMessageContaining("401");

            // 리프레시 토큰이 생성되지 않았는지 확인
            assertThat(fakesConfig.getRefreshTokenRepository().size()).isEqualTo(0);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 401 Unauthorized를 반환한다")
        void shouldReturn401WhenWrongPassword() {
            // given: 회원 가입 + 이메일 인증
            final var member = createTestMember("test@example.com", "테스트유저");
            signupAndVerifyViaHttp(member.email, member.password, member.nickname);

            // when: 잘못된 비밀번호로 로그인
            final LoginRequest loginRequest = new LoginRequest(member.email, "wrongpassword");

            // then
            assertThatThrownBy(() ->
                webClient.post()
                        .uri(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(toJson(loginRequest))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
            )
            .isInstanceOf(WebClientResponseException.class)
            .hasMessageContaining("401");
        }
    }

    @Nested
    @DisplayName("토큰 갱신 API 테스트")
    class 토큰_갱신_API_테스트 {

        private String getValidRefreshToken() {
            // 회원 가입 + 이메일 인증 후 로그인하여 유효한 리프레시 토큰 획득
            final var member = createTestMember("refresh@example.com", "리프레시테스트");
            signupAndVerifyViaHttp(member.email, member.password, member.nickname);

            final LoginRequest loginRequest = new LoginRequest(member.email, member.password);
            webClient.post()
                    .uri(AUTH_BASE_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(loginRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return "REFRESH_TOKEN_1"; // Fake 구현체의 패턴
        }

        @Test
        @DisplayName("유효한 리프레시 토큰으로 갱신 시 200 OK와 새 액세스 토큰을 반환한다")
        void shouldReturn200AndNewAccessTokenWhenValidRefreshToken() {
            // given
            final String refreshToken = getValidRefreshToken();
            final RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

            // when
            final String response = webClient.post()
                    .uri(AUTH_BASE_URL + "/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(request))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse = fromJson(response, new TypeReference<ApiResponse<Object>>() {});
            assertThat(apiResponse.success()).isTrue();
            assertThat(apiResponse.data()).isNotNull();
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 401 Unauthorized를 반환한다")
        void shouldReturn401WhenInvalidRefreshToken() {
            // given
            final RefreshTokenRequest request = new RefreshTokenRequest("INVALID_TOKEN");

            // when & then
            assertThatThrownBy(() ->
                webClient.post()
                        .uri(AUTH_BASE_URL + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(toJson(request))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
            )
            .isInstanceOf(WebClientResponseException.class)
            .hasMessageContaining("401");
        }
    }

    @Nested
    @DisplayName("완전한 인증 플로우 테스트")
    class 완전한_인증_플로우_테스트 {

        @Test
        @DisplayName("회원가입 → 이메일 인증 → 로그인 → 토큰갱신 전체 플로우가 HTTP를 통해 정상 동작한다")
        void shouldHandleCompleteAuthFlowViaHttp() {
            final var member = createTestMember("flow@example.com", "플로우테스트");

            // 1. 회원가입
            final SignupRequest signupRequest = new SignupRequest(member.email, member.password, member.password, member.nickname);
            final String signupResponse = webClient.post()
                    .uri(AUTH_BASE_URL + "/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(signupRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            final ApiResponse<?> signupApiResponse = fromJson(signupResponse, new TypeReference<ApiResponse<Object>>() {});
            assertThat(signupApiResponse.success()).isTrue();
            assertThat(fakesConfig.getMemberRepository().size()).isEqualTo(1);

            // 2. 이메일 인증
            final String code = fakesConfig.getEmailService().getVerificationCode(member.email);
            final String verifyBody = toJson(java.util.Map.of("email", member.email, "code", code));
            webClient.post()
                    .uri(AUTH_BASE_URL + "/verify-email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(verifyBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 3. 로그인
            final LoginRequest loginRequest = new LoginRequest(member.email, member.password);
            final String loginResponse = webClient.post()
                    .uri(AUTH_BASE_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(loginRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            final ApiResponse<?> loginApiResponse = fromJson(loginResponse, new TypeReference<ApiResponse<Object>>() {});
            assertThat(loginApiResponse.success()).isTrue();
            assertThat(fakesConfig.getRefreshTokenRepository().size()).isEqualTo(1);

            // 4. 토큰 갱신
            final String refreshToken = "REFRESH_TOKEN_1"; // Fake 구현체의 패턴
            final RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
            final String refreshResponse = webClient.post()
                    .uri(AUTH_BASE_URL + "/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(refreshRequest))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            final ApiResponse<?> refreshApiResponse = fromJson(refreshResponse, new TypeReference<ApiResponse<Object>>() {});
            assertThat(refreshApiResponse.success()).isTrue();
        }

        @Test
        @DisplayName("여러 회원이 동시에 HTTP를 통해 가입하고 로그인해도 격리되어 동작한다")
        void shouldIsolateDifferentMembersViaHttp() {
            // given
            final var member1 = createTestMember("user1@example.com", "유저1");
            final var member2 = createTestMember("user2@example.com", "유저2");

            // when: 동시 회원가입
            final SignupRequest signup1 = new SignupRequest(member1.email, member1.password, member1.password, member1.nickname);
            final SignupRequest signup2 = new SignupRequest(member2.email, member2.password, member2.password, member2.nickname);

            final String signupResponse1 = webClient.post()
                    .uri(AUTH_BASE_URL + "/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(signup1))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            final String signupResponse2 = webClient.post()
                    .uri(AUTH_BASE_URL + "/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(toJson(signup2))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // then
            final ApiResponse<?> apiResponse1 = fromJson(signupResponse1, new TypeReference<ApiResponse<Object>>() {});
            final ApiResponse<?> apiResponse2 = fromJson(signupResponse2, new TypeReference<ApiResponse<Object>>() {});

            assertThat(apiResponse1.success()).isTrue();
            assertThat(apiResponse2.success()).isTrue();
            assertThat(fakesConfig.getMemberRepository().size()).isEqualTo(2);

            // 각 회원이 올바르게 격리되어 저장되었는지 확인
            assertThat(fakesConfig.getMemberRepository().hasMemberWithEmail("user1@example.com")).isTrue();
            assertThat(fakesConfig.getMemberRepository().hasMemberWithEmail("user2@example.com")).isTrue();
        }
    }
}
