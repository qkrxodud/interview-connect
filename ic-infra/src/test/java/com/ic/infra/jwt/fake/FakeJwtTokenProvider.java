package com.ic.infra.jwt.fake;

import com.ic.domain.member.MemberRole;
import com.ic.infra.jwt.JwtTokenProvider;

/**
 * JWT Token Provider의 메모리 기반 Fake 구현체
 * - 실제 JWT 토큰을 생성하지 않고 테스트용 문자열 반환
 * - 빠른 테스트 실행
 * - 의존성 없는 단순한 구현
 */
public class FakeJwtTokenProvider extends JwtTokenProvider {

    public FakeJwtTokenProvider() {
        super(null); // JwtProperties 불필요
    }

    @Override
    public String generateAccessToken(Long memberId, MemberRole role) {
        return "ACCESS_TOKEN_" + memberId + "_" + role.name();
    }

    @Override
    public String generateRefreshToken(Long memberId) {
        return "REFRESH_TOKEN_" + memberId;
    }

    @Override
    public boolean validateToken(String token) {
        return token != null && (token.startsWith("REFRESH_TOKEN_") || token.startsWith("ACCESS_TOKEN_"));
    }

    @Override
    public Long getMemberIdFromToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token is null");
        }

        if (token.startsWith("REFRESH_TOKEN_")) {
            return Long.valueOf(token.replace("REFRESH_TOKEN_", ""));
        } else if (token.startsWith("ACCESS_TOKEN_")) {
            String[] parts = token.split("_");
            return Long.valueOf(parts[2]); // ACCESS_TOKEN_1_GENERAL -> 1
        }

        throw new IllegalArgumentException("Invalid token format");
    }

    @Override
    public MemberRole getMemberRoleFromToken(String token) {
        if (token == null || !token.startsWith("ACCESS_TOKEN_")) {
            throw new IllegalArgumentException("Invalid access token");
        }
        String[] parts = token.split("_");
        return MemberRole.valueOf(parts[parts.length - 1]);
    }

    @Override
    public long getTokenRemainingTime(String token) {
        return validateToken(token) ? 3600000L : 0L; // 1시간 또는 0
    }
}