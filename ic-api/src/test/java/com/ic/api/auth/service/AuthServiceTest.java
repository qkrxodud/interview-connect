package com.ic.api.auth.service;

import com.ic.api.auth.dto.LoginRequest;
import com.ic.api.auth.dto.SignupRequest;
import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.common.test.ServiceTestBase;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.member.MemberRole;
import com.ic.infra.jwt.JwtTokenProvider;
import com.ic.infra.redis.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthService 단위 테스트 (Mock 객체 사용)
 */
@DisplayName("AuthService 테스트")
class AuthServiceTest extends ServiceTestBase {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private Member member;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                memberRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider
        );

        signupRequest = new SignupRequest("test@example.com", "password123", "테스트");
        loginRequest = new LoginRequest("test@example.com", "password123");
        member = Member.createGeneral("test@example.com", "encodedPassword", "테스트");
    }

    @Nested
    @DisplayName("회원가입")
    class 회원가입 {

        @Test
        @DisplayName("회원가입 성공")
        void 회원가입_성공() {
            // given
            when(memberRepository.existsByEmail(signupRequest.email())).thenReturn(false);
            when(memberRepository.existsByNickname(signupRequest.nickname())).thenReturn(false);
            when(passwordEncoder.encode(signupRequest.password())).thenReturn("encodedPassword");
            when(memberRepository.save(any(Member.class))).thenReturn(
                Member.builder()
                    .id(1L)
                    .email(signupRequest.email())
                    .password("encodedPassword")
                    .nickname(signupRequest.nickname())
                    .role(MemberRole.GENERAL)
                    .build()
            );

            // when
            var response = authService.signup(signupRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.nickname()).isEqualTo("테스트");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 시 예외 발생")
        void 중복_이메일_회원가입_예외() {
            // given
            when(memberRepository.existsByEmail(signupRequest.email())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.DUPLICATE_EMAIL.getMessage());
        }

        @Test
        @DisplayName("중복 닉네임으로 회원가입 시 예외 발생")
        void 중복_닉네임_회원가입_예외() {
            // given
            when(memberRepository.existsByEmail(signupRequest.email())).thenReturn(false);
            when(memberRepository.existsByNickname(signupRequest.nickname())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(signupRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("이미 사용중인 닉네임입니다");
        }
    }

    @Nested
    @DisplayName("로그인")
    class 로그인 {

        @Test
        @DisplayName("로그인 성공")
        void 로그인_성공() {
            // given
            final Member savedMember = Member.builder()
                .id(1L)
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .role(member.getRole())
                .build();

            when(memberRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(savedMember));
            when(passwordEncoder.matches(loginRequest.password(), member.getPassword())).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(savedMember.getId(), savedMember.getRole())).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(savedMember.getId())).thenReturn("refreshToken");

            // when
            var response = authService.login(loginRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("accessToken");
            assertThat(response.refreshToken()).isEqualTo("refreshToken");
            assertThat(response.memberInfo().email()).isEqualTo("test@example.com");
            assertThat(response.memberInfo().role()).isEqualTo(MemberRole.GENERAL);
            verify(refreshTokenRepository).save(savedMember.getId(), "refreshToken");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void 존재하지_않는_이메일_로그인_예외() {
            // given
            when(memberRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void 잘못된_비밀번호_로그인_예외() {
            // given
            when(memberRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(loginRequest.password(), member.getPassword())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class 로그아웃 {

        @Test
        @DisplayName("로그아웃 성공")
        void 로그아웃_성공() {
            // given
            final Long memberId = 1L;

            // when
            authService.logout(memberId);

            // then
            verify(refreshTokenRepository).deleteByMemberId(memberId);
        }
    }
}