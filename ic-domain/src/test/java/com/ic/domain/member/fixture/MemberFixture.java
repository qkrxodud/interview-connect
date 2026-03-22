package com.ic.domain.member.fixture;

import com.github.javafaker.Faker;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRole;

import java.util.Locale;

/**
 * Member 엔티티 테스트 데이터 생성을 위한 Fixture 클래스
 */
public class MemberFixture {

    private static final Faker faker = new Faker(Locale.KOREA);

    /**
     * 일반회원 생성
     */
    public static Member 일반회원() {
        return Member.createGeneral(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().fullName()
        );
    }

    /**
     * 특정 이메일로 일반회원 생성
     */
    public static Member 일반회원(final String email) {
        return Member.createGeneral(
                email,
                faker.internet().password(),
                faker.name().fullName()
        );
    }

    /**
     * 특정 이메일과 닉네임으로 일반회원 생성
     */
    public static Member 일반회원(final String email, final String nickname) {
        return Member.createGeneral(
                email,
                faker.internet().password(),
                nickname
        );
    }

    /**
     * 비밀번호를 포함한 일반회원 생성
     */
    public static Member 일반회원_비밀번호포함(final String email, final String password) {
        return Member.createGeneral(
                email,
                password,
                faker.name().fullName()
        );
    }

    /**
     * 인증회원 생성
     */
    public static Member 인증회원() {
        final Member member = Member.createGeneral(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().fullName()
        );
        member.changeRole(MemberRole.VERIFIED);
        return member;
    }

    /**
     * 관리자 생성
     */
    public static Member 관리자() {
        final Member member = Member.createGeneral(
                faker.internet().emailAddress(),
                faker.internet().password(),
                faker.name().fullName()
        );
        member.changeRole(MemberRole.ADMIN);
        return member;
    }

    /**
     * ID가 설정된 일반회원 생성 (저장된 상태 모의)
     */
    public static Member 저장된_일반회원(final Long id) {
        final Member member = 일반회원();
        // reflection을 사용하지 않고 Builder 패턴으로 ID 설정
        return Member.builder()
                .id(id)
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .role(member.getRole())
                .build();
    }

    /**
     * 잘못된 데이터 생성을 위한 내부 클래스
     */
    public static class 잘못된_데이터 {

        public static String 너무_긴_이메일() {
            return "a".repeat(245) + "@example.com"; // 255자 초과
        }

        public static String 너무_긴_비밀번호() {
            return "password" + "a".repeat(250); // 255자 초과 (8 + 250 = 258자)
        }

        public static String 너무_긴_닉네임() {
            return "닉네임" + "가".repeat(50); // 30자 초과
        }

        public static String 잘못된_이메일_형식() {
            return "invalid-email-format";
        }

        public static String 짧은_비밀번호() {
            return "1234567"; // 8자 미만
        }

        public static String 짧은_닉네임() {
            return "a"; // 2자 미만
        }
    }
}