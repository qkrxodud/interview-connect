package com.ic.api.auth.service;

import com.ic.api.auth.dto.LoginRequest;
import com.ic.api.auth.dto.RefreshTokenRequest;
import com.ic.api.auth.dto.SignupRequest;
import com.ic.api.fake.FakeEmailService;
import com.ic.api.fake.FakeMemberRepository;
import com.ic.api.fake.FakePasswordEncoder;
import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;
import com.ic.infra.jwt.fake.FakeJwtTokenProvider;
import com.ic.infra.redis.fake.FakeRefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthService TDD 기반 테스트 (Fake 구현체 사용)
 * - Mock 대신 Fake 구현체로 빠른 테스트 실행
 * - 상태 기반 검증으로 명확한 테스트
 * - 실제 동작과 유사한 구현으로 신뢰성 향상
 */
@DisplayName("AuthService Fake 기반 테스트")
class AuthServiceFakeTest {

    // === Fake 구현체들 ===
    private FakeMemberRepository fakeMemberRepository;
    private FakeRefreshTokenRepository fakeRefreshTokenRepository;
    private FakePasswordEncoder fakePasswordEncoder;
    private FakeJwtTokenProvider fakeJwtTokenProvider;
    private FakeEmailService fakeEmailService;
    private AuthService authService;

    // === 테스트 데이터 ===
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // given: Fake 구현체들 초기화
        fakeMemberRepository = new FakeMemberRepository();
        fakeRefreshTokenRepository = new FakeRefreshTokenRepository();
        fakePasswordEncoder = new FakePasswordEncoder();
        fakeJwtTokenProvider = new FakeJwtTokenProvider();
        fakeEmailService = new FakeEmailService();

        authService = new AuthService(
                fakeMemberRepository,
                fakeRefreshTokenRepository,
                fakePasswordEncoder,
                fakeJwtTokenProvider,
                fakeEmailService
        );

        // 테스트 데이터 설정
        signupRequest = new SignupRequest("test@example.com", "password123", "password123", "테스트");
        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    /**
     * 회원가입 후 이메일 인증까지 완료하는 헬퍼
     * 로그인을 위해서는 이메일 인증이 필수이므로, 인증이 필요한 테스트에서 사용
     */
    private void signupAndVerify(final SignupRequest request) {
        authService.signup(request);
        final String code = fakeEmailService.getVerificationCode(request.email());
        authService.verifyEmail(request.email(), code);
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class 회원가입_테스트 {

        @Test
        @DisplayName("회원가입 성공 시 회원 정보와 인코딩된 비밀번호가 저장된다")
        void shouldSaveMemberWithEncodedPasswordWhenSignupSuccess() {
            // when
            final var response = authService.signup(signupRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.nickname()).isEqualTo("테스트");
            assertThat(response.id()).isNotNull();

            // 회원이 실제로 저장되었는지 확인
            assertThat(fakeMemberRepository.size()).isEqualTo(1);
            assertThat(fakeMemberRepository.hasMemberWithEmail("test@example.com")).isTrue();

            // 비밀번호가 인코딩되었는지 확인
            final var savedMember = fakeMemberRepository.findByEmail("test@example.com").get();
            assertThat(fakePasswordEncoder.isEncoded(savedMember.getPassword())).isTrue();
            assertThat(fakePasswordEncoder.matches("password123", savedMember.getPassword())).isTrue();
        }

        @Test
        @DisplayName("중복된 이메일로 회원가입 시 예외가 발생한다")
        void shouldThrowExceptionWhenDuplicateEmail() {
            // given: 기존 회원 등록
            authService.signup(signupRequest);

            // when & then
            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());

            // 회원이 중복 생성되지 않았는지 확인
            assertThat(fakeMemberRepository.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("중복된 닉네임으로 회원가입 시 예외가 발생한다")
        void shouldThrowExceptionWhenDuplicateNickname() {
            // given: 동일 닉네임의 기존 회원 등록
            authService.signup(signupRequest);
            final var anotherRequest = new SignupRequest("another@example.com", "password123", "password123", "테스트");

            // when & then
            assertThatThrownBy(() -> authService.signup(anotherRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("이미 사용중인 닉네임입니다");

            // 회원이 중복 생성되지 않았는지 확인
            assertThat(fakeMemberRepository.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class 로그인_테스트 {

        @BeforeEach
        void setUpLogin() {
            // given: 테스트용 회원 미리 가입 + 이메일 인증 완료
            signupAndVerify(signupRequest);
        }

        @Test
        @DisplayName("올바른 인증 정보로 로그인 시 토큰과 회원 정보를 반환한다")
        void shouldReturnTokensAndMemberInfoWhenValidCredentials() {
            // when
            final var response = authService.login(loginRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).startsWith("ACCESS_TOKEN_");
            assertThat(response.refreshToken()).startsWith("REFRESH_TOKEN_");
            assertThat(response.memberInfo().email()).isEqualTo("test@example.com");
            assertThat(response.memberInfo().nickname()).isEqualTo("테스트");
            assertThat(response.memberInfo().role()).isEqualTo(MemberRole.GENERAL);

            // 리프레시 토큰이 저장되었는지 확인
            final Long memberId = response.memberInfo().id();
            assertThat(fakeRefreshTokenRepository.hasToken(memberId)).isTrue();
            assertThat(fakeRefreshTokenRepository.findByMemberId(memberId))
                    .isPresent()
                    .hasValue(response.refreshToken());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
        void shouldThrowExceptionWhenEmailNotFound() {
            // given
            final var invalidRequest = new LoginRequest("unknown@example.com", "password123");

            // when & then
            assertThatThrownBy(() -> authService.login(invalidRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());

            // 리프레시 토큰이 생성되지 않았는지 확인
            assertThat(fakeRefreshTokenRepository.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidPassword() {
            // given
            final var invalidRequest = new LoginRequest("test@example.com", "wrongpassword");

            // when & then
            assertThatThrownBy(() -> authService.login(invalidRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());

            // 리프레시 토큰이 생성되지 않았는지 확인
            assertThat(fakeRefreshTokenRepository.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class 토큰_갱신_테스트 {

        private String validRefreshToken;
        private Long memberId;

        @BeforeEach
        void setUpTokenRefresh() {
            // given: 로그인하여 토큰 발급받기
            signupAndVerify(signupRequest);
            final var loginResponse = authService.login(loginRequest);
            validRefreshToken = loginResponse.refreshToken();
            memberId = loginResponse.memberInfo().id();
        }

        @Test
        @DisplayName("유효한 리프레시 토큰으로 액세스 토큰 갱신 시 새로운 액세스 토큰을 반환한다")
        void shouldReturnNewAccessTokenWhenValidRefreshToken() {
            // given
            final var refreshRequest = new RefreshTokenRequest(validRefreshToken);

            // when
            final var response = authService.refreshAccessToken(refreshRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).startsWith("ACCESS_TOKEN_");
            assertThat(response.accessToken()).contains(memberId.toString());

            // 저장된 리프레시 토큰은 그대로 유지되는지 확인
            assertThat(fakeRefreshTokenRepository.findByMemberId(memberId))
                    .isPresent()
                    .hasValue(validRefreshToken);
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 예외가 발생한다")
        void shouldThrowExceptionWhenInvalidRefreshToken() {
            // given
            final var invalidRequest = new RefreshTokenRequest("INVALID_TOKEN");

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(invalidRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.EXPIRED_TOKEN.getMessage());
        }

        @Test
        @DisplayName("Redis에 저장되지 않은 리프레시 토큰으로 갱신 시 예외가 발생한다")
        void shouldThrowExceptionWhenTokenNotStoredInRedis() {
            // given: 다른 회원 ID로 새 토큰 생성
            final Long anotherMemberId = 999L;
            final String anotherToken = fakeJwtTokenProvider.generateRefreshToken(anotherMemberId);
            final var invalidRequest = new RefreshTokenRequest(anotherToken);

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(invalidRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class 로그아웃_테스트 {

        @Test
        @DisplayName("로그아웃 시 저장된 리프레시 토큰이 삭제된다")
        void shouldDeleteRefreshTokenWhenLogout() {
            // given: 로그인하여 리프레시 토큰 생성
            signupAndVerify(signupRequest);
            final var loginResponse = authService.login(loginRequest);
            final Long memberId = loginResponse.memberInfo().id();

            // 리프레시 토큰이 저장되어 있는지 확인
            assertThat(fakeRefreshTokenRepository.hasToken(memberId)).isTrue();

            // when
            authService.logout(memberId);

            // then
            assertThat(fakeRefreshTokenRepository.hasToken(memberId)).isFalse();
            assertThat(fakeRefreshTokenRepository.findByMemberId(memberId)).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID로 로그아웃해도 예외가 발생하지 않는다")
        void shouldNotThrowExceptionWhenLogoutWithNonexistentMemberId() {
            // given
            final Long nonexistentMemberId = 999L;

            // when & then
            authService.logout(nonexistentMemberId); // 예외 발생하지 않음
            assertThat(fakeRefreshTokenRepository.hasToken(nonexistentMemberId)).isFalse();
        }
    }

    @Nested
    @DisplayName("복합 시나리오 테스트")
    class 복합_시나리오_테스트 {

        @Test
        @DisplayName("회원가입 → 로그인 → 토큰갱신 → 로그아웃 전체 플로우가 정상 동작한다")
        void shouldHandleCompleteAuthFlowSuccessfully() {
            // 1. 회원가입
            final var signupResponse = authService.signup(signupRequest);
            assertThat(signupResponse.email()).isEqualTo("test@example.com");
            assertThat(fakeMemberRepository.size()).isEqualTo(1);

            // 1-1. 이메일 인증
            final String verificationCode = fakeEmailService.getVerificationCode(signupRequest.email());
            authService.verifyEmail(signupRequest.email(), verificationCode);

            // 2. 로그인
            final var loginResponse = authService.login(loginRequest);
            final Long memberId = loginResponse.memberInfo().id();
            assertThat(loginResponse.accessToken()).isNotEmpty();
            assertThat(fakeRefreshTokenRepository.hasToken(memberId)).isTrue();

            // 3. 토큰 갱신
            final var refreshRequest = new RefreshTokenRequest(loginResponse.refreshToken());
            final var refreshResponse = authService.refreshAccessToken(refreshRequest);
            assertThat(refreshResponse.accessToken()).isNotEmpty();

            // 4. 로그아웃
            authService.logout(memberId);
            assertThat(fakeRefreshTokenRepository.hasToken(memberId)).isFalse();
        }

        @Test
        @DisplayName("여러 회원이 동시에 가입하고 로그인해도 격리되어 동작한다")
        void shouldIsolateDifferentMembersCorrectly() {
            // given: 두 명의 회원 가입
            final var member1Signup = new SignupRequest("user1@example.com", "password123", "password123", "유저1");
            final var member2Signup = new SignupRequest("user2@example.com", "password456", "password456", "유저2");

            signupAndVerify(member1Signup);
            signupAndVerify(member2Signup);

            // when: 각각 로그인
            final var member1Login = new LoginRequest("user1@example.com", "password123");
            final var member2Login = new LoginRequest("user2@example.com", "password456");

            final var loginResponse1 = authService.login(member1Login);
            final var loginResponse2 = authService.login(member2Login);

            // then: 각각의 토큰과 정보가 올바르게 분리되어 관리된다
            assertThat(fakeMemberRepository.size()).isEqualTo(2);
            assertThat(fakeRefreshTokenRepository.size()).isEqualTo(2);

            final Long member1Id = loginResponse1.memberInfo().id();
            final Long member2Id = loginResponse2.memberInfo().id();

            assertThat(member1Id).isNotEqualTo(member2Id);
            assertThat(loginResponse1.memberInfo().email()).isEqualTo("user1@example.com");
            assertThat(loginResponse2.memberInfo().email()).isEqualTo("user2@example.com");

            // 각각의 리프레시 토큰이 올바르게 저장되었는지 확인
            assertThat(fakeRefreshTokenRepository.findByMemberId(member1Id))
                    .isPresent()
                    .hasValue(loginResponse1.refreshToken());
            assertThat(fakeRefreshTokenRepository.findByMemberId(member2Id))
                    .isPresent()
                    .hasValue(loginResponse2.refreshToken());
        }
    }
}