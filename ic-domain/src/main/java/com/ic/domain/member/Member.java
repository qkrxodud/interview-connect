package com.ic.domain.member;

import com.ic.common.entity.BaseTimeEntity;
import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 회원 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // BCrypt 해시

    @Column(unique = true, nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(nullable = false)
    private Boolean emailVerified;

    @Column(length = 6)
    private String verificationCode;

    @Column
    private LocalDateTime verificationCodeExpiry;

    @Builder
    private Member(Long id, String email, String password, String nickname, MemberRole role,
                  Boolean emailVerified, String verificationCode, LocalDateTime verificationCodeExpiry) {
        validateEmail(email);
        validatePassword(password);
        validateNickname(nickname);

        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Objects.nonNull(role) ? role : MemberRole.GENERAL;
        this.emailVerified = Objects.nonNull(emailVerified) ? emailVerified : false;
        this.verificationCode = verificationCode;
        this.verificationCodeExpiry = verificationCodeExpiry;
    }

    /**
     * 회원가입을 위한 정적 팩터리 메서드
     */
    public static Member createGeneral(String email, String password, String nickname) {
        return Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(MemberRole.GENERAL)
                .build();
    }

    /**
     * 회원 등급 변경
     */
    public void changeRole(MemberRole newRole) {
        if (Objects.isNull(newRole)) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.role = newRole;
    }

    /**
     * 닉네임 변경
     */
    public void changeNickname(String newNickname) {
        validateNickname(newNickname);
        this.nickname = newNickname;
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        validatePassword(newPassword);
        this.password = newPassword;
    }

    /**
     * 인증 회원 여부 확인
     */
    public boolean isVerified() {
        return role.isVerified();
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return role.isAdmin();
    }

    /**
     * 일반 회원 여부 확인
     */
    public boolean isGeneral() {
        return role == MemberRole.GENERAL;
    }

    /**
     * 질문 작성 권한 확인
     */
    public boolean canAskQuestion() {
        return role.canAskQuestion();
    }

    /**
     * 후기 작성 권한 확인
     */
    public boolean canWriteReview() {
        return role.canWriteReview();
    }

    /**
     * Q&A 답변 권한 확인
     */
    public boolean canAnswerQuestion() {
        return role.canAnswerQuestion();
    }

    /**
     * 이메일 인증 코드 변경 (1시간 후 만료)
     */
    public void changeVerificationCode(final String code) {
        this.verificationCode = code;
        this.verificationCodeExpiry = LocalDateTime.now().plusHours(1);
    }

    /**
     * 이메일 인증 처리
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.verificationCode = null;
        this.verificationCodeExpiry = null;
    }

    /**
     * 인증 코드 만료 여부 확인
     */
    public boolean isVerificationCodeExpired() {
        if (Objects.isNull(verificationCodeExpiry)) {
            return true;
        }
        return LocalDateTime.now().isAfter(verificationCodeExpiry);
    }

    /**
     * 인증 코드 일치 여부 확인
     */
    public boolean isVerificationCodeValid(String code) {
        if (Objects.isNull(verificationCode) || Objects.isNull(code)) {
            return false;
        }
        return verificationCode.equals(code) && !isVerificationCodeExpired();
    }

    /**
     * 이메일 인증 여부 확인
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    // 검증 메서드들

    private void validateEmail(String email) {
        if (Objects.isNull(email) || email.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!email.contains("@")) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "올바른 이메일 형식이 아닙니다");
        }
        if (email.length() > 100) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "이메일은 100자 이하로 입력해주세요");
        }
    }

    private void validatePassword(String password) {
        if (Objects.isNull(password) || password.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (password.length() < 8) {
            throw BusinessException.from(ErrorCode.INVALID_PASSWORD);
        }
        if (password.length() > 255) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 255자 이하로 입력해주세요");
        }
    }

    private void validateNickname(String nickname) {
        if (Objects.isNull(nickname) || nickname.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (nickname.length() > 30) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "닉네임은 30자 이하로 입력해주세요");
        }
        if (nickname.length() < 2) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT_VALUE, "닉네임은 2자 이상 입력해주세요");
        }
    }
}