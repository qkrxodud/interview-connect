package com.ic.domain.member;

import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 회원 도메인 서비스
 * 회원과 관련된 비즈니스 로직을 처리한다
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * ID로 회원 조회
     */
    public Member findById(Long memberId) {
        if (Objects.isNull(memberId)) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 이메일로 회원 조회
     */
    public Member findByEmail(String email) {
        if (Objects.isNull(email) || email.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.from(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 이메일 중복 확인
     */
    public void validateEmailNotDuplicated(String email) {
        if (Objects.isNull(email) || email.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (memberRepository.existsByEmail(email)) {
            throw BusinessException.from(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    /**
     * 닉네임 중복 확인
     */
    public void validateNicknameNotDuplicated(String nickname) {
        if (Objects.isNull(nickname) || nickname.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw BusinessException.from(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    /**
     * 회원 등급 변경
     */
    @Transactional
    public void changeRole(Long memberId, MemberRole newRole) {
        if (Objects.isNull(memberId) || Objects.isNull(newRole)) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }

        final Member member = findById(memberId);
        member.changeRole(newRole);

        log.info("Member role changed: memberId={}, oldRole={}, newRole={}",
                memberId, member.getRole(), newRole);
    }

    /**
     * 회원 인증 처리 (GENERAL -> VERIFIED)
     */
    @Transactional
    public void verifyMember(Long memberId) {
        final Member member = findById(memberId);

        if (member.isVerified()) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "이미 인증된 회원입니다");
        }

        member.changeRole(MemberRole.VERIFIED);

        log.info("Member verified: memberId={}, email={}", memberId, member.getEmail());
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public void changeNickname(Long memberId, String newNickname) {
        if (Objects.isNull(memberId) || Objects.isNull(newNickname) || newNickname.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }

        final Member member = findById(memberId);

        // 현재 닉네임과 동일한지 확인
        if (member.getNickname().equals(newNickname)) {
            return; // 변경사항이 없으면 그대로 리턴
        }

        // 새 닉네임 중복 확인
        validateNicknameNotDuplicated(newNickname);

        member.changeNickname(newNickname);

        log.info("Member nickname changed: memberId={}, newNickname={}", memberId, newNickname);
    }

    /**
     * 회원 권한 확인 (인증 회원 여부)
     */
    public void validateVerifiedMember(Long memberId) {
        final Member member = findById(memberId);
        if (!member.isVerified()) {
            throw BusinessException.from(ErrorCode.REVIEW_PERMISSION_DENIED);
        }
    }

    /**
     * 관리자 권한 확인
     */
    public void validateAdminMember(Long memberId) {
        final Member member = findById(memberId);
        if (!member.isAdmin()) {
            throw BusinessException.from(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 회원 존재 여부 확인
     */
    public boolean existsById(Long memberId) {
        if (Objects.isNull(memberId)) {
            return false;
        }
        return memberRepository.existsById(memberId);
    }

    /**
     * 이메일로 회원 존재 여부 확인
     */
    public boolean existsByEmail(String email) {
        if (Objects.isNull(email) || email.trim().isEmpty()) {
            return false;
        }
        return memberRepository.existsByEmail(email);
    }
}