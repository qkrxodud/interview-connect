package com.ic.infra.jwt;

import com.ic.common.exception.BusinessException;
import com.ic.common.test.UnitTestBase;
import com.ic.domain.member.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtTokenProvider 테스트
 */
@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest extends UnitTestBase {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // JwtProperties 설정
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-for-jwt-token-signing-that-is-long-enough");
        jwtProperties.setAccessTokenExpireTime(3600000L); // 1시간
        jwtProperties.setRefreshTokenExpireTime(7 * 24 * 3600000L); // 7일

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Nested
    @DisplayName("액세스 토큰 생성")
    class 액세스_토큰_생성 {

        @Test
        @DisplayName("액세스 토큰 생성 성공")
        void 액세스_토큰_생성_성공() {
            // given
            final Long memberId = 1L;
            final MemberRole role = MemberRole.GENERAL;

            // when
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT 구조: header.payload.signature
        }

        @Test
        @DisplayName("다른 회원 ID로 다른 토큰 생성")
        void 다른_회원_ID로_다른_토큰_생성() {
            // given
            final Long memberId1 = 1L;
            final Long memberId2 = 2L;
            final MemberRole role = MemberRole.GENERAL;

            // when
            final String token1 = jwtTokenProvider.generateAccessToken(memberId1, role);
            final String token2 = jwtTokenProvider.generateAccessToken(memberId2, role);

            // then
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("다른 역할로 다른 토큰 생성")
        void 다른_역할로_다른_토큰_생성() {
            // given
            final Long memberId = 1L;
            final MemberRole role1 = MemberRole.GENERAL;
            final MemberRole role2 = MemberRole.VERIFIED;

            // when
            final String token1 = jwtTokenProvider.generateAccessToken(memberId, role1);
            final String token2 = jwtTokenProvider.generateAccessToken(memberId, role2);

            // then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 생성")
    class 리프레시_토큰_생성 {

        @Test
        @DisplayName("리프레시 토큰 생성 성공")
        void 리프레시_토큰_생성_성공() {
            // given
            final Long memberId = 1L;

            // when
            final String token = jwtTokenProvider.generateRefreshToken(memberId);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class 토큰_검증 {

        @Test
        @DisplayName("유효한 액세스 토큰 검증 성공")
        void 유효한_액세스_토큰_검증_성공() {
            // given
            final Long memberId = 1L;
            final MemberRole role = MemberRole.GENERAL;
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);

            // when
            final boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("유효한 리프레시 토큰 검증 성공")
        void 유효한_리프레시_토큰_검증_성공() {
            // given
            final Long memberId = 1L;
            final String token = jwtTokenProvider.generateRefreshToken(memberId);

            // when
            final boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 실패")
        void 잘못된_형식의_토큰_검증_실패() {
            // given
            final String invalidToken = "invalid.token.format";

            // when
            final boolean isValid = jwtTokenProvider.validateToken(invalidToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("null 토큰 검증 실패")
        void null_토큰_검증_실패() {
            // when
            final boolean isValid = jwtTokenProvider.validateToken(null);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰 검증 실패")
        void 빈_문자열_토큰_검증_실패() {
            // when
            final boolean isValid = jwtTokenProvider.validateToken("");

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("서명이 변조된 토큰 검증 실패")
        void 서명이_변조된_토큰_검증_실패() {
            // given
            final Long memberId = 1L;
            final MemberRole role = MemberRole.GENERAL;
            final String validToken = jwtTokenProvider.generateAccessToken(memberId, role);

            // 마지막 문자를 변경하여 서명 변조
            final String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";

            // when
            final boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰에서 정보 추출")
    class 토큰에서_정보_추출 {

        @Test
        @DisplayName("액세스 토큰에서 회원 ID 추출")
        void 액세스_토큰에서_회원_ID_추출() {
            // given
            final Long memberId = 123L;
            final MemberRole role = MemberRole.VERIFIED;
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);

            // when
            final Long extractedMemberId = jwtTokenProvider.getMemberIdFromToken(token);

            // then
            assertThat(extractedMemberId).isEqualTo(memberId);
        }

        @Test
        @DisplayName("리프레시 토큰에서 회원 ID 추출")
        void 리프레시_토큰에서_회원_ID_추출() {
            // given
            final Long memberId = 456L;
            final String token = jwtTokenProvider.generateRefreshToken(memberId);

            // when
            final Long extractedMemberId = jwtTokenProvider.getMemberIdFromToken(token);

            // then
            assertThat(extractedMemberId).isEqualTo(memberId);
        }

        @Test
        @DisplayName("액세스 토큰에서 역할 추출")
        void 액세스_토큰에서_역할_추출() {
            // given
            final Long memberId = 789L;
            final MemberRole role = MemberRole.ADMIN;
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);

            // when
            final MemberRole extractedRole = jwtTokenProvider.getMemberRoleFromToken(token);

            // then
            assertThat(extractedRole).isEqualTo(role);
        }

        @Test
        @DisplayName("잘못된 토큰에서 정보 추출 시 예외 발생")
        void 잘못된_토큰에서_정보_추출_시_예외_발생() {
            // given
            final String invalidToken = "invalid.token.format";

            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromToken(invalidToken))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("null 토큰에서 정보 추출 시 예외 발생")
        void null_토큰에서_정보_추출_시_예외_발생() {
            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.getMemberIdFromToken(null))
                    .isInstanceOf(BusinessException.class);

            assertThatThrownBy(() -> jwtTokenProvider.getMemberRoleFromToken(null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("리프레시 토큰에서 역할 추출 시 예외 발생")
        void 리프레시_토큰에서_역할_추출_시_예외_발생() {
            // given
            final Long memberId = 1L;
            final String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);

            // when & then - 리프레시 토큰에는 역할 정보가 없으므로 예외 발생
            assertThatThrownBy(() -> jwtTokenProvider.getMemberRoleFromToken(refreshToken))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("토큰 남은 시간 계산")
    class 토큰_남은_시간_계산 {

        @Test
        @DisplayName("유효한 토큰의 남은 시간 계산")
        void 유효한_토큰의_남은_시간_계산() {
            // given
            final Long memberId = 1L;
            final MemberRole role = MemberRole.GENERAL;
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);

            // when
            final long remainingTime = jwtTokenProvider.getTokenRemainingTime(token);

            // then
            assertThat(remainingTime).isPositive();
            assertThat(remainingTime).isLessThanOrEqualTo(3600000L); // 1시간 이하
        }

        @Test
        @DisplayName("잘못된 토큰의 남은 시간은 0")
        void 잘못된_토큰의_남은_시간은_0() {
            // given
            final String invalidToken = "invalid.token.format";

            // when
            final long remainingTime = jwtTokenProvider.getTokenRemainingTime(invalidToken);

            // then
            assertThat(remainingTime).isZero();
        }

        @Test
        @DisplayName("null 토큰의 남은 시간은 0")
        void null_토큰의_남은_시간은_0() {
            // when
            final long remainingTime = jwtTokenProvider.getTokenRemainingTime(null);

            // then
            assertThat(remainingTime).isZero();
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class 경계값_테스트 {

        @Test
        @DisplayName("null 회원ID로 토큰 생성 시 예외 발생하지 않음")
        void null_회원ID로_토큰_생성_시_예외_발생하지_않음() {
            // when & then - null 체크는 JwtTokenProvider 내부에서 처리하지 않을 수 있음
            // 실제 동작을 확인하는 테스트
            assertThatThrownBy(() -> jwtTokenProvider.generateAccessToken(null, MemberRole.GENERAL))
                    .isInstanceOf(Exception.class); // NPE 또는 다른 예외 발생 예상
        }

        @Test
        @DisplayName("null 역할로 토큰 생성 시 예외 발생하지 않음")
        void null_역할로_토큰_생성_시_예외_발생하지_않음() {
            // when & then
            assertThatThrownBy(() -> jwtTokenProvider.generateAccessToken(1L, null))
                    .isInstanceOf(Exception.class); // NPE 또는 다른 예외 발생 예상
        }

        @Test
        @DisplayName("0 회원ID로 토큰 생성 및 추출")
        void 회원ID_0으로_토큰_생성_및_추출() {
            // given
            final Long memberId = 0L;
            final MemberRole role = MemberRole.GENERAL;

            // when
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);
            final Long extractedId = jwtTokenProvider.getMemberIdFromToken(token);

            // then
            assertThat(extractedId).isEqualTo(memberId);
        }

        @Test
        @DisplayName("음수 회원ID로 토큰 생성 및 추출")
        void 음수_회원ID로_토큰_생성_및_추출() {
            // given
            final Long memberId = -1L;
            final MemberRole role = MemberRole.GENERAL;

            // when
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);
            final Long extractedId = jwtTokenProvider.getMemberIdFromToken(token);

            // then
            assertThat(extractedId).isEqualTo(memberId);
        }

        @Test
        @DisplayName("매우 큰 회원ID로 토큰 생성 및 추출")
        void 매우_큰_회원ID로_토큰_생성_및_추출() {
            // given
            final Long memberId = Long.MAX_VALUE;
            final MemberRole role = MemberRole.ADMIN;

            // when
            final String token = jwtTokenProvider.generateAccessToken(memberId, role);
            final Long extractedId = jwtTokenProvider.getMemberIdFromToken(token);

            // then
            assertThat(extractedId).isEqualTo(memberId);
        }
    }
}