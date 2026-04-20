package com.ic.api.auth.service;

import com.ic.api.auth.dto.*;
import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import com.ic.domain.member.EmailService;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.member.MemberRole;
import com.ic.infra.jwt.JwtTokenProvider;
import com.ic.infra.redis.RefreshToken;
import com.ic.infra.redis.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 * - 회원가입, 로그인, 토큰 갱신, 로그아웃 기능 제공
 * - JWT 토큰 기반 인증 처리
 * - Redis를 통한 리프레시 토큰 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    /**
     * 회원가입을 처리합니다.
     *
     * @param request 회원가입 요청 정보
     * @return 가입된 회원 정보
     * @throws BusinessException 이메일 또는 닉네임이 중복된 경우
     */
    @Transactional
    public SignupResponse signup(final SignupRequest request) {
        validateDuplicateEmail(request.email());
        validateDuplicateNickname(request.nickname());

        final String encodedPassword = passwordEncoder.encode(request.password());
        final Member member = Member.createGeneral(request.email(), encodedPassword, request.nickname());

        // 이메일 인증 코드 생성 및 설정
        final String verificationCode = emailService.generateVerificationCode();
        member.setVerificationCode(verificationCode);

        final Member savedMember = memberRepository.save(member);

        // 인증 이메일 발송
        emailService.sendVerificationEmail(savedMember.getEmail(), verificationCode);

        return createSignupResponse(savedMember);
    }

    /**
     * 로그인을 처리합니다.
     *
     * @param request 로그인 요청 정보
     * @return 액세스 토큰, 리프레시 토큰 및 회원 정보
     * @throws BusinessException 인증에 실패한 경우
     */
    @Transactional
    public LoginResponse login(final LoginRequest request) {
        final Member member = findMemberByEmail(request.email());
        validatePassword(request.password(), member.getPassword());

        // 이메일 인증 여부 확인
        if (!member.isEmailVerified()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "이메일 인증이 완료되지 않았습니다. 이메일을 확인해주세요.");
        }

        final String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole());
        final String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId());

        refreshTokenRepository.save(member.getId(), refreshToken);

        return createLoginResponse(accessToken, refreshToken, member);
    }

    /**
     * 리프레시 토큰을 이용하여 액세스 토큰을 갱신합니다.
     *
     * @param request 토큰 갱신 요청 정보
     * @return 새로운 액세스 토큰
     * @throws BusinessException 토큰이 유효하지 않은 경우
     */
    public RefreshTokenResponse refreshAccessToken(final RefreshTokenRequest request) {
        final String refreshToken = request.refreshToken();

        validateRefreshToken(refreshToken);
        final Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
        validateStoredRefreshToken(memberId, refreshToken);

        final Member member = findMemberById(memberId);
        final String newAccessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole());

        return new RefreshTokenResponse(newAccessToken);
    }

    /**
     * 로그아웃을 처리합니다.
     * Redis에 저장된 리프레시 토큰을 삭제합니다.
     *
     * @param memberId 회원 ID
     */
    @Transactional
    public void logout(final Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    /**
     * 이메일 인증을 처리합니다.
     *
     * @param email 이메일
     * @param code 인증 코드
     * @throws BusinessException 인증 코드가 유효하지 않은 경우
     */
    @Transactional
    public void verifyEmail(final String email, final String code) {
        final Member member = findMemberByEmail(email);

        if (!member.isVerificationCodeValid(code)) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "인증 코드가 유효하지 않거나 만료되었습니다.");
        }

        member.verifyEmail();
        memberRepository.save(member);
    }

    /**
     * 인증 코드를 재발송합니다.
     *
     * @param email 이메일
     * @throws BusinessException 회원이 존재하지 않거나 이미 인증된 경우
     */
    @Transactional
    public void resendVerificationCode(final String email) {
        final Member member = findMemberByEmail(email);

        if (member.isEmailVerified()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "이미 인증된 이메일입니다.");
        }

        final String newVerificationCode = emailService.generateVerificationCode();
        member.setVerificationCode(newVerificationCode);
        memberRepository.save(member);

        emailService.sendVerificationEmail(email, newVerificationCode);
    }

    // === Private Helper Methods ===

    /**
     * 이메일 중복을 검사합니다.
     */
    private void validateDuplicateEmail(final String email) {
        if (memberRepository.existsByEmail(email)) {
            throw BusinessException.from(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    /**
     * 닉네임 중복을 검사합니다.
     */
    private void validateDuplicateNickname(final String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw BusinessException.of(ErrorCode.DUPLICATE_NICKNAME, "이미 사용중인 닉네임입니다");
        }
    }

    /**
     * 비밀번호를 검증합니다.
     */
    private void validatePassword(final String rawPassword, final String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw BusinessException.from(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    /**
     * 리프레시 토큰의 유효성을 검사합니다.
     */
    private void validateRefreshToken(final String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException.from(ErrorCode.EXPIRED_TOKEN);
        }
    }

    /**
     * Redis에 저장된 리프레시 토큰을 검증합니다.
     */
    private void validateStoredRefreshToken(final Long memberId, final String refreshToken) {
        final String storedToken = refreshTokenRepository.findByMemberId(memberId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.INVALID_TOKEN));

        if (!storedToken.equals(refreshToken)) {
            throw BusinessException.from(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 이메일로 회원을 조회합니다.
     */
    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> BusinessException.from(ErrorCode.INVALID_CREDENTIALS));
    }

    /**
     * ID로 회원을 조회합니다.
     */
    private Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 회원가입 응답을 생성합니다.
     */
    private SignupResponse createSignupResponse(final Member member) {
        return new SignupResponse(
            member.getId(),
            member.getEmail(),
            member.getNickname()
        );
    }

    /**
     * 로그인 응답을 생성합니다.
     */
    private LoginResponse createLoginResponse(final String accessToken, final String refreshToken, final Member member) {
        return new LoginResponse(
            accessToken,
            refreshToken,
            new LoginResponse.MemberInfo(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole()
            )
        );
    }
}