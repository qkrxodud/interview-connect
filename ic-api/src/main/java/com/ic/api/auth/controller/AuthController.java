package com.ic.api.auth.controller;

import com.ic.api.auth.dto.*;
import com.ic.api.auth.service.AuthService;
import com.ic.api.config.security.AuthMember;
import com.ic.api.config.security.CustomUserDetails;
import com.ic.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@RequestBody @Valid SignupRequest request) {
        final SignupResponse response = authService.signup(request);
        return ApiResponse.ok(response);
    }

    /**
     * 회원가입 (별칭)
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> register(@RequestBody @Valid SignupRequest request) {
        final SignupResponse response = authService.signup(request);
        return ApiResponse.ok(response);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        final LoginResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshAccessToken(@RequestBody @Valid RefreshTokenRequest request) {
        final RefreshTokenResponse response = authService.refreshAccessToken(request);
        return ApiResponse.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            final Long memberId = Long.valueOf(authentication.getName());
            authService.logout(memberId);
        }
        return ApiResponse.ok();
    }

    /**
     * 이메일 인증
     */
    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestBody @Valid EmailVerificationRequest request) {
        authService.verifyEmail(request.email(), request.code());
        return ApiResponse.ok();
    }

    /**
     * 인증 코드 재발송
     */
    @PostMapping("/resend-code")
    public ApiResponse<Void> resendVerificationCode(@RequestBody @Valid ResendCodeRequest request) {
        authService.resendVerificationCode(request.email());
        return ApiResponse.ok();
    }

    /**
     * 현재 로그인한 회원 정보 조회 (@AuthMember 어노테이션 테스트용)
     */
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getProfile(@AuthMember CustomUserDetails userDetails) {
        final ProfileResponse profile = new ProfileResponse(
            userDetails.getMemberId(),
            userDetails.getEmail(),
            userDetails.getNickname(),
            userDetails.getRole()
        );
        return ApiResponse.ok(profile);
    }

    /**
     * 프로필 응답 DTO
     */
    public record ProfileResponse(
        Long id,
        String email,
        String nickname,
        com.ic.domain.member.MemberRole role
    ) {
    }
}