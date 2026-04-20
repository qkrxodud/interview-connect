package com.ic.api.test;

import com.ic.domain.company.Company;
import com.ic.domain.company.CompanyRepository;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.review.InterviewReviewRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 테스트용 고정 데이터(Fixture) 생성 및 관리
 * - 일관된 테스트 환경 제공
 * - 복잡한 데이터 관계 설정
 * - 재사용 가능한 테스트 시나리오
 */
@Component
public class TestFixture {

    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final InterviewReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public TestFixture(MemberRepository memberRepository,
                      CompanyRepository companyRepository,
                      InterviewReviewRepository reviewRepository,
                      PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.companyRepository = companyRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 기본 테스트 환경 구성
     * - 기본 회원 1명
     * - 기본 회사 3개
     */
    public TestEnvironment setupBasicEnvironment() {
        final Member member = createAndSaveTestMember("test@example.com", "테스트유저");
        final Company kakao = createAndSaveTestCompany("카카오", "IT");
        final Company naver = createAndSaveTestCompany("네이버", "IT");
        final Company samsung = createAndSaveTestCompany("삼성전자", "제조");

        return new TestEnvironment(member, kakao, naver, samsung);
    }

    /**
     * 멀티 유저 테스트 환경 구성
     * - 일반 회원, 인증 회원, 관리자
     * - 다양한 업계의 회사들
     */
    public MultiUserTestEnvironment setupMultiUserEnvironment() {
        // 다양한 역할의 회원들
        final Member generalMember = createAndSaveTestMember("general@example.com", "일반유저");
        final Member verifiedMember = memberRepository.save(
                TestDataFactory.createVerifiedMember("verified@example.com", "인증유저")
        );
        final Member adminMember = memberRepository.save(
                TestDataFactory.createAdminMember("admin@example.com", "관리자")
        );

        // 다양한 업계의 회사들
        final Company itCompany = createAndSaveTestCompany("네이버", "IT");
        final Company financeCompany = createAndSaveTestCompany("KB국민은행", "금융");
        final Company manufacturingCompany = createAndSaveTestCompany("현대자동차", "제조");

        return new MultiUserTestEnvironment(
                generalMember, verifiedMember, adminMember,
                itCompany, financeCompany, manufacturingCompany
        );
    }

    /**
     * 회사 검색 테스트 환경 구성
     * - 검색 기능 테스트에 특화된 데이터
     */
    public CompanySearchTestEnvironment setupCompanySearchEnvironment() {
        // 검색 테스트용 회사들
        final Company kakao = createAndSaveTestCompany("카카오", "IT");
        final Company kakaoBank = createAndSaveTestCompany("카카오뱅크", "금융");
        final Company kakaoGames = createAndSaveTestCompany("카카오게임즈", "게임");
        final Company naver = createAndSaveTestCompany("네이버", "IT");
        final Company naverZ = createAndSaveTestCompany("네이버Z", "메타버스");

        return new CompanySearchTestEnvironment(kakao, kakaoBank, kakaoGames, naver, naverZ);
    }

    /**
     * 인증 플로우 테스트 환경 구성
     * - 로그인/회원가입 테스트에 특화
     */
    public AuthTestEnvironment setupAuthTestEnvironment() {
        // 기존 가입된 회원 (로그인 테스트용)
        final Member existingMember = createAndSaveTestMember("existing@example.com", "기존유저");

        return new AuthTestEnvironment(existingMember);
    }

    // === Private Helper Methods ===

    private Member createAndSaveTestMember(String email, String nickname) {
        final String encodedPassword = passwordEncoder.encode("password123");
        final Member member = Member.createGeneral(email, encodedPassword, nickname);
        return memberRepository.save(member);
    }

    private Company createAndSaveTestCompany(String name, String industry) {
        final Company company = TestDataFactory.createTestCompany(name, industry);
        return companyRepository.save(company);
    }

    // === Test Environment Classes ===

    /**
     * 기본 테스트 환경 정보
     */
    public static class TestEnvironment {
        public final Member member;
        public final Company kakao;
        public final Company naver;
        public final Company samsung;

        public TestEnvironment(Member member, Company kakao, Company naver, Company samsung) {
            this.member = member;
            this.kakao = kakao;
            this.naver = naver;
            this.samsung = samsung;
        }
    }

    /**
     * 멀티 유저 테스트 환경 정보
     */
    public static class MultiUserTestEnvironment {
        public final Member generalMember;
        public final Member verifiedMember;
        public final Member adminMember;
        public final Company itCompany;
        public final Company financeCompany;
        public final Company manufacturingCompany;

        public MultiUserTestEnvironment(Member generalMember, Member verifiedMember, Member adminMember,
                                       Company itCompany, Company financeCompany, Company manufacturingCompany) {
            this.generalMember = generalMember;
            this.verifiedMember = verifiedMember;
            this.adminMember = adminMember;
            this.itCompany = itCompany;
            this.financeCompany = financeCompany;
            this.manufacturingCompany = manufacturingCompany;
        }
    }

    /**
     * 회사 검색 테스트 환경 정보
     */
    public static class CompanySearchTestEnvironment {
        public final Company kakao;
        public final Company kakaoBank;
        public final Company kakaoGames;
        public final Company naver;
        public final Company naverZ;

        public CompanySearchTestEnvironment(Company kakao, Company kakaoBank, Company kakaoGames,
                                          Company naver, Company naverZ) {
            this.kakao = kakao;
            this.kakaoBank = kakaoBank;
            this.kakaoGames = kakaoGames;
            this.naver = naver;
            this.naverZ = naverZ;
        }
    }

    /**
     * 인증 테스트 환경 정보
     */
    public static class AuthTestEnvironment {
        public final Member existingMember;

        public AuthTestEnvironment(Member existingMember) {
            this.existingMember = existingMember;
        }
    }
}