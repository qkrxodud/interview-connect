package com.ic.domain.member;

/**
 * 회원 등급 정의
 */
public enum MemberRole {

    /**
     * 일반 회원 - 회원가입한 모든 사용자
     * 후기/Q&A 조회, 질문 작성 가능
     */
    GENERAL("일반"),

    /**
     * 인증 회원 - 재직 확인을 완료한 회원
     * 후기 작성, Q&A 답변, 1:1 채팅 가능
     */
    VERIFIED("인증"),

    /**
     * 관리자 - 시스템 관리 권한
     * 회사 등록, 회원 관리, 콘텐츠 관리 가능
     */
    ADMIN("관리자");

    private final String displayName;

    MemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 인증 회원 여부 확인
     */
    public boolean isVerified() {
        return this == VERIFIED || this == ADMIN;
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 질문 작성 권한 확인
     * 모든 등급 가능
     */
    public boolean canAskQuestion() {
        return true;
    }

    /**
     * 후기 작성 권한 확인
     * 인증 회원, 관리자만 가능
     */
    public boolean canWriteReview() {
        return this == VERIFIED || this == ADMIN;
    }

    /**
     * Q&A 답변 권한 확인
     * 인증 회원, 관리자만 가능
     */
    public boolean canAnswerQuestion() {
        return this == VERIFIED || this == ADMIN;
    }
}