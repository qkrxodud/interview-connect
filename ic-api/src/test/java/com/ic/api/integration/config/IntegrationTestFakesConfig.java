package com.ic.api.integration.config;

import com.ic.api.fake.*;
import com.ic.api.test.DatabaseCleaner;
import com.ic.domain.member.EmailService;
import com.ic.infra.jwt.JwtTokenProvider;
import com.ic.infra.jwt.fake.FakeJwtTokenProvider;
import com.ic.infra.redis.RefreshTokenRepository;
import com.ic.infra.redis.fake.FakeRefreshTokenRepository;
import com.ic.domain.company.CompanyRepository;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.notification.NotificationRepository;
import com.ic.domain.qa.ReviewAnswerRepository;
import com.ic.domain.qa.ReviewQuestionRepository;
import com.ic.domain.review.InterviewReviewRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 통합 테스트용 Fake 구현체 설정
 * - 모든 외부 의존성을 Fake로 대체
 * - Spring Boot 컨텍스트 공유로 성능 향상
 * - 테스트 간 데이터 격리를 위한 reset 메서드 제공
 */
@TestConfiguration
public class IntegrationTestFakesConfig {

    // === Repository Fake 구현체들 ===

    private final FakeMemberRepository fakeMemberRepository = new FakeMemberRepository();
    private final FakeCompanyRepository fakeCompanyRepository = new FakeCompanyRepository();
    private final FakeInterviewReviewRepository fakeInterviewReviewRepository = new FakeInterviewReviewRepository();
    private final FakeReviewQuestionRepository fakeReviewQuestionRepository = new FakeReviewQuestionRepository();
    private final FakeReviewAnswerRepository fakeReviewAnswerRepository = new FakeReviewAnswerRepository();
    private final FakeNotificationRepository fakeNotificationRepository = new FakeNotificationRepository();

    // === Infra Fake 구현체들 ===

    private final FakeRefreshTokenRepository fakeRefreshTokenRepository = new FakeRefreshTokenRepository();
    private final FakeJwtTokenProvider fakeJwtTokenProvider = new FakeJwtTokenProvider();
    private final FakePasswordEncoder fakePasswordEncoder = new FakePasswordEncoder();
    private final FakeEmailService fakeEmailService = new FakeEmailService();

    // === Repository Bean 등록 ===

    // JPA가 "memberRepository" 등 동일한 이름의 빈을 생성하므로
    // 이름 충돌을 피하기 위해 "fake" 접두어를 사용
    // @Primary로 타입 기반 주입 시 Fake가 선택되도록 보장

    @Bean("fakeMemberRepository")
    @Primary
    public MemberRepository memberRepository() {
        return fakeMemberRepository;
    }

    @Bean("fakeCompanyRepository")
    @Primary
    public CompanyRepository companyRepository() {
        return fakeCompanyRepository;
    }

    @Bean("fakeInterviewReviewRepository")
    @Primary
    public InterviewReviewRepository interviewReviewRepository() {
        return fakeInterviewReviewRepository;
    }

    @Bean("fakeReviewQuestionRepository")
    @Primary
    public ReviewQuestionRepository reviewQuestionRepository() {
        return fakeReviewQuestionRepository;
    }

    @Bean("fakeReviewAnswerRepository")
    @Primary
    public ReviewAnswerRepository reviewAnswerRepository() {
        return fakeReviewAnswerRepository;
    }

    @Bean("fakeNotificationRepository")
    @Primary
    public NotificationRepository notificationRepository() {
        return fakeNotificationRepository;
    }

    // === Infra Bean 등록 ===

    @Bean
    @Primary
    public RefreshTokenRepository refreshTokenRepository() {
        return fakeRefreshTokenRepository;
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        return fakeJwtTokenProvider;
    }

    @Bean("fakePasswordEncoder")
    @Primary
    public PasswordEncoder passwordEncoder() {
        return fakePasswordEncoder;
    }

    @Bean
    @Primary
    public EmailService emailService() {
        return fakeEmailService;
    }

    // === 테스트 유틸리티 Bean 등록 ===

    @Bean
    public DatabaseCleaner databaseCleaner() {
        return new DatabaseCleaner(
            fakeMemberRepository,
            fakeCompanyRepository,
            fakeInterviewReviewRepository,
            fakeReviewQuestionRepository,
            fakeReviewAnswerRepository,
            fakeNotificationRepository
        );
    }

    // === 테스트 헬퍼 메서드들 ===

    /**
     * 모든 Fake 구현체 초기화
     * 테스트 간 데이터 격리를 위해 사용
     */
    public void resetAllFakes() {
        // Repository 초기화
        fakeMemberRepository.clear();
        fakeCompanyRepository.clear();
        fakeInterviewReviewRepository.clear();
        fakeReviewQuestionRepository.clear();
        fakeReviewAnswerRepository.clear();
        fakeNotificationRepository.clear();

        // Infra 초기화
        fakeRefreshTokenRepository.clear();
        fakeEmailService.clear();
        // JwtTokenProvider와 PasswordEncoder는 상태가 없으므로 초기화 불필요
    }

    /**
     * 개별 Fake 구현체 접근용 Getter들
     */
    public FakeMemberRepository getMemberRepository() {
        return fakeMemberRepository;
    }

    public FakeCompanyRepository getCompanyRepository() {
        return fakeCompanyRepository;
    }

    public FakeInterviewReviewRepository getInterviewReviewRepository() {
        return fakeInterviewReviewRepository;
    }

    public FakeReviewQuestionRepository getReviewQuestionRepository() {
        return fakeReviewQuestionRepository;
    }

    public FakeReviewAnswerRepository getReviewAnswerRepository() {
        return fakeReviewAnswerRepository;
    }

    public FakeNotificationRepository getNotificationRepository() {
        return fakeNotificationRepository;
    }

    public FakeRefreshTokenRepository getRefreshTokenRepository() {
        return fakeRefreshTokenRepository;
    }

    public FakeJwtTokenProvider getJwtTokenProvider() {
        return fakeJwtTokenProvider;
    }

    public FakePasswordEncoder getPasswordEncoder() {
        return fakePasswordEncoder;
    }

    public FakeEmailService getEmailService() {
        return fakeEmailService;
    }
}