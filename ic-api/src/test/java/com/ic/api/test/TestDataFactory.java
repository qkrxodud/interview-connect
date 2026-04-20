package com.ic.api.test;

import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;

/**
 * 테스트 데이터 생성을 위한 팩터리 클래스
 * - 일관된 테스트 데이터 생성
 * - 중복 코드 제거
 * - 테스트 가독성 향상
 */
public final class TestDataFactory {

    private TestDataFactory() {
        // 유틸리티 클래스는 인스턴스 생성 방지
    }

    // === Member 테스트 데이터 ===

    /**
     * 기본 테스트 회원 생성
     */
    public static Member createTestMember() {
        return createTestMember("test@example.com", "테스트유저");
    }

    /**
     * 커스텀 이메일과 닉네임을 가진 테스트 회원 생성
     */
    public static Member createTestMember(String email, String nickname) {
        return Member.createGeneral(email, "FAKE_ENCODED_password123", nickname);
    }

    /**
     * 특정 ID를 가진 테스트 회원 생성
     */
    public static Member createTestMemberWithId(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .password("FAKE_ENCODED_password123")
                .nickname(nickname)
                .role(MemberRole.GENERAL)
                .build();
    }

    /**
     * 인증된 회원 생성
     */
    public static Member createVerifiedMember(String email, String nickname) {
        return Member.builder()
                .email(email)
                .password("FAKE_ENCODED_password123")
                .nickname(nickname)
                .role(MemberRole.VERIFIED)
                .build();
    }

    /**
     * 관리자 회원 생성
     */
    public static Member createAdminMember(String email, String nickname) {
        return Member.builder()
                .email(email)
                .password("FAKE_ENCODED_password123")
                .nickname(nickname)
                .role(MemberRole.ADMIN)
                .build();
    }

    // === Company 테스트 데이터 ===

    /**
     * 기본 테스트 회사 생성
     */
    public static Company createTestCompany() {
        return createTestCompany("테스트컴퍼니", "IT");
    }

    /**
     * 커스텀 이름과 업계를 가진 테스트 회사 생성
     */
    public static Company createTestCompany(String name, String industry) {
        return Company.from(name, industry, null, null);
    }

    /**
     * 특정 ID를 가진 테스트 회사 생성
     */
    public static Company createTestCompanyWithId(Long id, String name, String industry) {
        return Company.builder()
                .id(id)
                .name(name)
                .industry(industry)
                .logoUrl(null)
                .website(null)
                .build();
    }

    /**
     * 완전한 정보를 가진 테스트 회사 생성
     */
    public static Company createFullTestCompany(String name, String industry, String logoUrl, String website) {
        return Company.from(name, industry, logoUrl, website);
    }

    // === 자주 사용되는 테스트 회사들 ===

    public static Company createKakaoCompany() {
        return createFullTestCompany(
                "카카오",
                "IT",
                "https://example.com/kakao-logo.png",
                "https://www.kakaocorp.com"
        );
    }

    public static Company createNaverCompany() {
        return createFullTestCompany(
                "네이버",
                "IT",
                "https://example.com/naver-logo.png",
                "https://www.navercorp.com"
        );
    }

    public static Company createSamsungCompany() {
        return createFullTestCompany(
                "삼성전자",
                "제조",
                "https://example.com/samsung-logo.png",
                "https://www.samsung.com"
        );
    }

    // === JWT 토큰 관련 ===

    /**
     * 테스트용 액세스 토큰 생성 패턴
     */
    public static String createTestAccessToken(Long memberId, MemberRole role) {
        return "ACCESS_TOKEN_" + memberId + "_" + role.name();
    }

    /**
     * 테스트용 리프레시 토큰 생성 패턴
     */
    public static String createTestRefreshToken(Long memberId) {
        return "REFRESH_TOKEN_" + memberId;
    }

    // === 이메일 관련 ===

    /**
     * 고유한 테스트 이메일 생성 (시간 기반)
     */
    public static String createUniqueEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * 도메인별 테스트 이메일 생성
     */
    public static String createEmailWithDomain(String prefix, String domain) {
        return prefix + "@" + domain;
    }

    // === 닉네임 관련 ===

    /**
     * 고유한 테스트 닉네임 생성
     */
    public static String createUniqueNickname() {
        return "테스트유저" + System.currentTimeMillis();
    }

    /**
     * 패턴 기반 닉네임 생성
     */
    public static String createNicknameWithPattern(String pattern, int number) {
        return pattern + number;
    }
}