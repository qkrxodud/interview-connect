package com.ic.domain.fixture;

import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;

/**
 * Member 엔티티 테스트 픽스처
 */
public class MemberFixture {

    public static Member 일반회원() {
        return Member.builder()
                .email("general@example.com")
                .password("password123")
                .nickname("일반회원")
                .role(MemberRole.GENERAL)
                .build();
    }

    public static Member 인증회원() {
        return Member.builder()
                .email("verified@example.com")
                .password("password123")
                .nickname("인증회원")
                .role(MemberRole.VERIFIED)
                .build();
    }

    public static Member 관리자() {
        return Member.builder()
                .email("admin@example.com")
                .password("password123")
                .nickname("관리자")
                .role(MemberRole.ADMIN)
                .build();
    }

    public static Member 회원_생성(String email, String nickname, MemberRole role) {
        return Member.builder()
                .email(email)
                .password("password123")
                .nickname(nickname)
                .role(role)
                .build();
    }

    public static Member ID가_있는_인증회원(Long id) {
        return Member.builder()
                .id(id)
                .email("verified@example.com")
                .password("password123")
                .nickname("인증회원")
                .role(MemberRole.VERIFIED)
                .build();
    }
}