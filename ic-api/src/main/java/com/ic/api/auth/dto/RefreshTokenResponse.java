package com.ic.api.auth.dto;

/**
 * 토큰 갱신 응답 DTO
 */
public record RefreshTokenResponse(
        String accessToken
) {
}