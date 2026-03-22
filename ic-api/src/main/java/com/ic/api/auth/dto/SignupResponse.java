package com.ic.api.auth.dto;

/**
 * 회원가입 응답 DTO
 */
public record SignupResponse(
        Long id,
        String email,
        String nickname
) {
}