package com.ic.api.auth.dto;

import com.ic.domain.member.MemberRole;

/**
 * 로그인 응답 DTO
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        MemberInfo memberInfo
) {
    public record MemberInfo(
            Long id,
            String email,
            String nickname,
            MemberRole role
    ) {
    }
}