package com.ic.domain.member;

import com.ic.common.entity.BaseTimeEntity;
import com.ic.common.exception.BusinessException;
import com.ic.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Builder
    private Member(Long id, String email, String password, String nickname, MemberRole role) {
        validateEmail(email);
        validatePassword(password);
        validateNickname(nickname);

        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Objects.nonNull(role) ? role : MemberRole.GENERAL;
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
            throw BusinessException.from(ErrorCode.INVALID_INPUT);
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

    // 검증 메서드들

    private void validateEmail(String email) {
        if (Objects.isNull(email) || email.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT);
        }
        if (!email.contains("@")) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT, "올바른 이메일 형식이 아닙니다");
        }
        if (email.length() > 100) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT, "이메일은 100자 이하로 입력해주세요");
        }
    }

    private void validatePassword(String password) {
        if (Objects.isNull(password) || password.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT);
        }
        if (password.length() < 8) {
            throw BusinessException.from(ErrorCode.INVALID_PASSWORD);
        }
        if (password.length() > 255) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT, "비밀번호는 255자 이하로 입력해주세요");
        }
    }

    private void validateNickname(String nickname) {
        if (Objects.isNull(nickname) || nickname.trim().isEmpty()) {
            throw BusinessException.from(ErrorCode.INVALID_INPUT);
        }
        if (nickname.length() > 30) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT, "닉네임은 30자 이하로 입력해주세요");
        }
        if (nickname.length() < 2) {
            throw BusinessException.of(ErrorCode.INVALID_INPUT, "닉네임은 2자 이상 입력해주세요");
        }
    }
}