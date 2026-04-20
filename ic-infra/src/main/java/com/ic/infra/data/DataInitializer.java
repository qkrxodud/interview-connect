package com.ic.infra.data;

import com.ic.domain.company.Company;
import com.ic.domain.company.CompanyRepository;
import com.ic.domain.member.Member;
import com.ic.domain.member.MemberRepository;
import com.ic.domain.member.MemberRole;
import com.ic.domain.qa.ReviewAnswer;
import com.ic.domain.qa.ReviewAnswerRepository;
import com.ic.domain.qa.ReviewQuestion;
import com.ic.domain.qa.ReviewQuestionRepository;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

/**
 * 개발용 초기 데이터 생성기
 * 로컬 및 개발 환경에서만 실행되며, 회사/회원/후기/Q&A 씨드 데이터를 생성한다
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"local", "dev"})
@EnableAsync
public class DataInitializer {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final InterviewReviewRepository reviewRepository;
    private final ReviewQuestionRepository questionRepository;
    private final ReviewAnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void initData() {
        log.info("Application ready - starting seed data initialization...");

        // 스키마 생성 완료 대기
        sleepQuietly(500);

        int retryCount = 0;
        final int maxRetries = 5;

        while (retryCount < maxRetries) {
            try {
                initAllSeedData();
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("Attempt {} failed to initialize seed data: {}", retryCount, e.getMessage());

                if (retryCount < maxRetries) {
                    sleepQuietly(1000L * retryCount);
                } else {
                    log.error("Failed to initialize seed data after {} attempts. Tables may not be available.", maxRetries);
                }
            }
        }
    }

    private void initAllSeedData() {
        if (companyRepository.count() == 0) {
            initCompanies();
        } else {
            log.info("Company seed data already exists, skipping");
        }

        initTestMembers();

        if (reviewRepository.count() == 0) {
            initReviews();
        } else {
            log.info("Review seed data already exists, skipping");
        }

        if (questionRepository.count() == 0) {
            initQuestionsAndAnswers();
        } else {
            log.info("Q&A seed data already exists, skipping");
        }

        log.info("Seed data initialization completed successfully");
    }

    // ===== 회원 초기화 =====

    private void initTestMembers() {
        createMemberIfNotExists("verified@test.com", "Test1234!", "후기작성자", MemberRole.VERIFIED);
        createMemberIfNotExists("general@test.com", "Test1234!", "질문자", MemberRole.GENERAL);
        log.info("Test member initialization completed");
    }

    private void createMemberIfNotExists(String email, String rawPassword, String nickname, MemberRole role) {
        if (memberRepository.existsByEmail(email)) {
            log.info("Test member already exists: {}", email);
            return;
        }

        final String encodedPassword = passwordEncoder.encode(rawPassword);
        final Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(role)
                .emailVerified(true)
                .build();
        memberRepository.save(member);
        log.info("Test member created: {} ({})", email, role.getDisplayName());
    }

    // ===== 후기 초기화 =====

    private void initReviews() {
        log.info("Initializing interview review seed data...");

        final Member verifiedMember = memberRepository.findByEmail("verified@test.com")
                .orElseThrow(() -> new IllegalStateException("Verified test member not found"));

        final List<ReviewData> reviewDataList = buildReviewDataList();

        reviewDataList.forEach(data -> {
            final Company company = companyRepository.findByName(data.companyName())
                    .orElseThrow(() -> new IllegalStateException("Company not found: " + data.companyName()));

            final InterviewReview review = InterviewReview.create(
                    verifiedMember,
                    company,
                    data.interviewDate(),
                    data.position(),
                    data.interviewTypes(),
                    data.questions(),
                    data.difficulty(),
                    data.atmosphere(),
                    data.result(),
                    data.content()
            );
            reviewRepository.save(review);
        });

        log.info("Interview review seed data initialized. Total reviews: {}", reviewDataList.size());
    }

    private List<ReviewData> buildReviewDataList() {
        return List.of(
                // 카카오 (4건)
                new ReviewData("카카오", "백엔드 개발자", InterviewResult.PASS, 4, 4,
                        LocalDate.of(2024, 1, 15),
                        List.of("기술면접", "인성면접"),
                        List.of("Spring Boot에서 트랜잭션 전파 옵션에 대해 설명해주세요",
                                "JPA N+1 문제 해결 방법은 무엇인가요?",
                                "동시성 이슈를 처리한 경험이 있다면 말씀해주세요"),
                        "카카오 백엔드 면접은 기술 깊이를 많이 보는 편이었습니다. 코딩 테스트 통과 후 1차 기술면접에서 스프링 핵심 원리와 JPA 최적화에 대해 깊이 물어봤습니다. 2차에서는 시스템 설계와 인성 질문이 혼합되어 나왔고, 전반적으로 편안한 분위기에서 진행되었습니다."),

                new ReviewData("카카오", "프론트엔드 개발자", InterviewResult.FAIL, 5, 3,
                        LocalDate.of(2024, 3, 10),
                        List.of("기술면접"),
                        List.of("React의 Virtual DOM 동작 원리를 설명해주세요",
                                "웹 성능 최적화 경험을 공유해주세요",
                                "상태 관리 라이브러리 비교 분석을 해주세요"),
                        "카카오 프론트엔드 면접 난이도가 상당히 높았습니다. 단순히 사용법이 아니라 내부 동작 원리까지 꼬리 질문이 이어졌습니다. 특히 번들링과 렌더링 최적화 관련 질문이 까다로웠고, 실제 프로젝트 경험을 깊이 파고들었습니다."),

                new ReviewData("카카오", "데이터엔지니어", InterviewResult.PASS, 4, 4,
                        LocalDate.of(2024, 5, 22),
                        List.of("기술면접", "인성면접"),
                        List.of("Spark와 Hadoop의 차이점은 무엇인가요?",
                                "데이터 파이프라인 설계 경험을 말씀해주세요",
                                "데이터 품질 관리는 어떻게 하시나요?"),
                        "카카오 데이터엔지니어 면접은 대규모 데이터 처리 경험을 중점적으로 봤습니다. Spark 튜닝 경험과 실시간 파이프라인 구축에 대한 질문이 많았고, 면접관분들이 친절하게 힌트도 주셔서 좋은 분위기였습니다."),

                new ReviewData("카카오", "SRE 엔지니어", InterviewResult.PENDING, 3, 4,
                        LocalDate.of(2024, 7, 8),
                        List.of("기술면접"),
                        List.of("Kubernetes 운영 경험에 대해 말씀해주세요",
                                "장애 대응 프로세스를 설명해주세요",
                                "모니터링 시스템을 어떻게 구축하시나요?"),
                        "카카오 SRE 면접은 인프라 운영 경험 위주로 진행되었습니다. Kubernetes 클러스터 관리, CI/CD 파이프라인 구성, 장애 대응 사례 등을 물어봤고, 면접관들이 실무에서 겪는 상황을 공유하며 대화 형식으로 진행했습니다."),

                // 네이버 (4건)
                new ReviewData("네이버", "백엔드 개발자", InterviewResult.PASS, 5, 5,
                        LocalDate.of(2024, 2, 20),
                        List.of("기술면접", "인성면접"),
                        List.of("대규모 트래픽 처리 경험이 있으신가요?",
                                "Redis 캐시 전략에 대해 설명해주세요",
                                "마이크로서비스 아키텍처의 장단점은 무엇인가요?"),
                        "네이버 백엔드 면접은 대규모 서비스 운영 경험을 중시했습니다. 초당 수만 건의 요청을 처리하는 아키텍처 설계에 대해 깊이 있는 대화를 나눴고, 면접관분들이 매우 전문적이면서도 편안한 분위기를 만들어주셨습니다."),

                new ReviewData("네이버", "데이터엔지니어", InterviewResult.PENDING, 4, 3,
                        LocalDate.of(2024, 4, 5),
                        List.of("기술면접"),
                        List.of("ETL 파이프라인 설계 방법론을 설명해주세요",
                                "데이터 레이크와 데이터 웨어하우스의 차이는?",
                                "실시간 스트리밍 처리 경험이 있으신가요?"),
                        "네이버 데이터엔지니어 면접은 꽤 체계적이었습니다. 데이터 아키텍처 전반에 대한 이해도를 확인했고, 특히 Kafka를 활용한 실시간 처리와 배치 처리의 트레이드오프에 대해 심도 깊게 논의했습니다."),

                new ReviewData("네이버", "프론트엔드 개발자", InterviewResult.PASS, 4, 5,
                        LocalDate.of(2024, 6, 18),
                        List.of("기술면접", "인성면접"),
                        List.of("Next.js SSR과 CSR의 차이를 설명해주세요",
                                "접근성(A11y)을 고려한 개발 경험이 있나요?",
                                "대규모 프론트엔드 프로젝트 구조를 어떻게 설계하시나요?"),
                        "네이버 프론트엔드 면접은 체계적이고 분위기가 정말 좋았습니다. 기술적인 질문뿐 아니라 사용자 경험과 접근성에 대한 고민도 많이 물어봤고, 면접관분들의 피드백이 건설적이어서 면접 자체가 좋은 경험이었습니다."),

                new ReviewData("네이버", "DevOps 엔지니어", InterviewResult.FAIL, 5, 4,
                        LocalDate.of(2024, 8, 12),
                        List.of("기술면접"),
                        List.of("IaC 도구 사용 경험에 대해 말씀해주세요",
                                "무중단 배포 전략을 설명해주세요",
                                "컨테이너 오케스트레이션 경험을 공유해주세요"),
                        "네이버 DevOps 면접은 난이도가 높았습니다. Terraform, Ansible 등 IaC 도구 경험과 함께 네트워크 기본기에 대한 질문이 깊었습니다. 특히 카나리 배포와 블루-그린 배포의 실무 적용 사례를 요구했는데 경험이 부족했습니다."),

                // 토스 (4건)
                new ReviewData("토스", "백엔드 개발자", InterviewResult.PASS, 5, 5,
                        LocalDate.of(2024, 3, 25),
                        List.of("기술면접", "인성면접"),
                        List.of("분산 트랜잭션 처리 방법을 설명해주세요",
                                "결제 시스템에서 멱등성을 어떻게 보장하나요?",
                                "이벤트 소싱 패턴의 장단점은 무엇인가요?"),
                        "토스 백엔드 면접은 금융 도메인 특성상 안정성과 정확성에 대한 질문이 많았습니다. 분산 환경에서의 데이터 일관성 보장, 장애 복구 전략 등 실무적인 질문이 인상적이었고, 면접관들의 기술 수준이 매우 높다고 느꼈습니다."),

                new ReviewData("토스", "iOS 개발자", InterviewResult.PASS, 4, 4,
                        LocalDate.of(2024, 5, 30),
                        List.of("기술면접"),
                        List.of("Swift의 메모리 관리 방식을 설명해주세요",
                                "MVVM 패턴 적용 경험을 공유해주세요",
                                "앱 성능 최적화 경험이 있다면 말씀해주세요"),
                        "토스 iOS 면접은 Swift 언어 자체에 대한 깊은 이해와 아키텍처 설계 능력을 중점적으로 봤습니다. 코드 리뷰 세션이 있었는데 실제 코드를 보면서 개선점을 논의하는 형태가 인상적이었습니다."),

                new ReviewData("토스", "서버 개발자", InterviewResult.FAIL, 5, 4,
                        LocalDate.of(2024, 9, 3),
                        List.of("기술면접", "인성면접"),
                        List.of("CQRS 패턴을 적용한 경험이 있나요?",
                                "DB 샤딩 전략에 대해 설명해주세요",
                                "대용량 알림 시스템을 설계해보세요"),
                        "토스 서버 개발자 면접에서 시스템 설계 문제가 나왔는데 난이도가 매우 높았습니다. 실시간 알림 시스템 설계를 요구했는데, 초당 수백만 건 처리 규모의 설계를 요구해서 경험 부족으로 제대로 답변하지 못했습니다."),

                new ReviewData("토스", "QA 엔지니어", InterviewResult.PASS, 3, 5,
                        LocalDate.of(2024, 11, 15),
                        List.of("기술면접", "인성면접"),
                        List.of("테스트 자동화 전략을 설명해주세요",
                                "성능 테스트 경험을 공유해주세요",
                                "버그 리포팅 프로세스를 어떻게 개선하셨나요?"),
                        "토스 QA 엔지니어 면접은 예상보다 편안하고 대화 중심이었습니다. 테스트 자동화 프레임워크 구축 경험과 품질 문화에 대한 가치관을 많이 물어봤고, 면접 과정에서 토스의 품질에 대한 열정을 느낄 수 있었습니다."),

                // 배달의민족 (4건)
                new ReviewData("배달의민족", "백엔드 개발자", InterviewResult.FAIL, 4, 4,
                        LocalDate.of(2024, 4, 12),
                        List.of("기술면접", "인성면접"),
                        List.of("Spring WebFlux와 MVC의 차이는 무엇인가요?",
                                "주문 시스템에서 동시성 제어를 어떻게 하나요?",
                                "DDD를 적용한 경험이 있다면 말씀해주세요"),
                        "배달의민족 백엔드 면접은 도메인 주도 설계에 대한 이해를 많이 봤습니다. 주문-결제-배달 도메인 간의 경계 설정과 이벤트 기반 통신에 대해 깊이 물어봤고, 기술면접 후 인성면접에서 협업 경험을 중점적으로 확인했습니다."),

                new ReviewData("배달의민족", "인프라 엔지니어", InterviewResult.PASS, 3, 4,
                        LocalDate.of(2024, 6, 28),
                        List.of("기술면접"),
                        List.of("AWS 인프라 구축 경험을 공유해주세요",
                                "로드밸런서 설정과 오토스케일링 전략은?",
                                "비용 최적화 경험이 있다면 말씀해주세요"),
                        "배민 인프라 엔지니어 면접은 AWS 실무 경험 위주였습니다. 클라우드 아키텍처 설계와 비용 최적화에 대한 질문이 많았고, 실제 장애 상황 시나리오를 주고 대응 방안을 물어보는 형태가 인상적이었습니다."),

                new ReviewData("배달의민족", "안드로이드 개발자", InterviewResult.PASS, 4, 5,
                        LocalDate.of(2024, 10, 7),
                        List.of("기술면접", "인성면접"),
                        List.of("Kotlin Coroutine의 동작 원리를 설명해주세요",
                                "Compose와 기존 View 시스템의 차이는?",
                                "앱 아키텍처 설계 철학을 공유해주세요"),
                        "배민 안드로이드 면접은 Kotlin과 Jetpack Compose에 대한 질문이 많았습니다. 면접관분들이 굉장히 친절하시고, 모르는 부분에 대해서는 같이 고민하는 분위기였습니다. 팀 문화에 대한 설명도 자세히 해주셔서 좋았습니다."),

                new ReviewData("배달의민족", "ML 엔지니어", InterviewResult.PENDING, 5, 3,
                        LocalDate.of(2024, 12, 2),
                        List.of("기술면접"),
                        List.of("추천 시스템 설계 경험을 말씀해주세요",
                                "모델 서빙 아키텍처를 어떻게 구성하시나요?",
                                "A/B 테스트 설계 방법론을 설명해주세요"),
                        "배민 ML 엔지니어 면접은 추천 알고리즘과 모델 서빙에 초점이 맞춰져 있었습니다. 논문 기반 질문보다는 실무 적용과 성능 개선 경험을 더 중시했고, 면접 난이도는 상당히 높은 편이었습니다."),

                // 쿠팡 (4건)
                new ReviewData("쿠팡", "백엔드 개발자", InterviewResult.PASS, 4, 3,
                        LocalDate.of(2024, 2, 8),
                        List.of("기술면접"),
                        List.of("HashMap의 내부 구현을 설명해주세요",
                                "REST API 설계 원칙을 말씀해주세요",
                                "대규모 데이터 정렬 알고리즘을 설계해보세요"),
                        "쿠팡 백엔드 면접은 자료구조와 알고리즘에 대한 기본기를 철저히 확인했습니다. 코딩 테스트 2회에 온사이트 면접까지 총 3단계로 진행되었고, 시스템 디자인 문제에서 이커머스 도메인 특성을 반영한 설계를 요구했습니다."),

                new ReviewData("쿠팡", "데이터분석가", InterviewResult.FAIL, 3, 3,
                        LocalDate.of(2024, 7, 19),
                        List.of("기술면접", "인성면접"),
                        List.of("SQL 쿼리 최적화 경험을 공유해주세요",
                                "A/B 테스트 설계와 분석 방법은?",
                                "비즈니스 지표 정의 경험을 말씀해주세요"),
                        "쿠팡 데이터분석가 면접은 SQL 실력과 비즈니스 인사이트 도출 능력을 봤습니다. 실제 데이터셋을 기반으로 분석 과제가 주어졌고, 분석 결과를 비개발자에게 설명하는 능력도 평가했습니다. 면접 분위기는 다소 딱딱한 편이었습니다."),

                new ReviewData("쿠팡", "풀스택 개발자", InterviewResult.PASS, 4, 4,
                        LocalDate.of(2024, 9, 25),
                        List.of("기술면접", "인성면접"),
                        List.of("프론트엔드와 백엔드 간 API 설계 원칙은?",
                                "CI/CD 파이프라인 구축 경험을 공유해주세요",
                                "코드 리뷰 문화에 대한 생각을 말씀해주세요"),
                        "쿠팡 풀스택 면접은 프론트와 백엔드 모두 고루 물어봤습니다. TypeScript와 Java를 함께 다루는 역량을 확인했고, 특히 API 설계와 에러 핸들링에 대한 일관된 철학이 있는지 중점적으로 봤습니다."),

                new ReviewData("쿠팡", "보안 엔지니어", InterviewResult.PASS, 4, 3,
                        LocalDate.of(2024, 12, 20),
                        List.of("기술면접"),
                        List.of("OWASP Top 10 취약점을 설명해주세요",
                                "침투 테스트 경험을 공유해주세요",
                                "보안 사고 대응 프로세스를 설명해주세요"),
                        "쿠팡 보안 엔지니어 면접은 웹 보안과 인프라 보안에 대한 폭넓은 지식을 요구했습니다. OWASP 관련 질문부터 시작해서 실제 취약점 분석 시나리오까지 진행했고, 보안 관제 시스템 구축 경험도 물어봤습니다.")
        );
    }

    // ===== Q&A 초기화 =====

    private void initQuestionsAndAnswers() {
        log.info("Initializing Q&A seed data...");

        final Member generalMember = memberRepository.findByEmail("general@test.com")
                .orElseThrow(() -> new IllegalStateException("General test member not found"));
        final Member verifiedMember = memberRepository.findByEmail("verified@test.com")
                .orElseThrow(() -> new IllegalStateException("Verified test member not found"));

        final List<InterviewReview> reviews = reviewRepository.findAll();
        if (reviews.size() < 5) {
            log.warn("Not enough reviews to create Q&A data. Found: {}", reviews.size());
            return;
        }

        // 처음 5건의 후기에 Q&A 추가
        final List<QaData> qaDataList = buildQaDataList();

        for (int i = 0; i < Math.min(5, reviews.size()); i++) {
            final InterviewReview review = reviews.get(i);
            final QaData qaData = qaDataList.get(i);

            qaData.questions().forEach(questionContent -> {
                final ReviewQuestion question = ReviewQuestion.create(review, generalMember, questionContent);
                questionRepository.save(question);

                // 각 질문에 답변 1~2개 추가
                final String answerContent = generateAnswer(review.getCompany().getName(), questionContent);
                final ReviewAnswer answer = ReviewAnswer.create(question, verifiedMember, answerContent);
                answerRepository.save(answer);
            });
        }

        log.info("Q&A seed data initialized for first 5 reviews");
    }

    private List<QaData> buildQaDataList() {
        return List.of(
                // 카카오 백엔드 후기 Q&A (5개 질문)
                new QaData(List.of(
                        "코딩 테스트는 어떤 유형의 문제가 나왔나요?",
                        "면접 시간은 얼마나 걸렸나요?",
                        "기술 면접에서 라이브 코딩도 있었나요?",
                        "연봉 협상은 어떻게 진행되었나요?",
                        "합격 후 온보딩 과정은 어떤가요?"
                )),
                // 카카오 프론트엔드 후기 Q&A (4개 질문)
                new QaData(List.of(
                        "포트폴리오 준비는 어떻게 하셨나요?",
                        "React 외에 다른 프레임워크 경험도 물어보나요?",
                        "불합격 이유가 무엇이라고 생각하시나요?",
                        "재지원 가능 기간은 어떻게 되나요?"
                )),
                // 카카오 데이터엔지니어 후기 Q&A (3개 질문)
                new QaData(List.of(
                        "SQL 관련 질문도 있었나요?",
                        "데이터 파이프라인 관련 과제가 있었나요?",
                        "팀 분위기는 어떤 편인가요?"
                )),
                // 카카오 SRE 후기 Q&A (4개 질문)
                new QaData(List.of(
                        "Kubernetes 자격증이 필수인가요?",
                        "온콜 근무 빈도는 어떤가요?",
                        "면접 준비 팁이 있다면 알려주세요",
                        "어떤 모니터링 도구 경험을 원하나요?"
                )),
                // 네이버 백엔드 후기 Q&A (5개 질문)
                new QaData(List.of(
                        "신입도 지원 가능한 포지션인가요?",
                        "기술 스택은 Spring만 사용하나요?",
                        "코딩 테스트 언어 제한이 있나요?",
                        "면접 복장은 어떻게 하셨나요?",
                        "입사 후 팀 배치는 어떻게 되나요?"
                ))
        );
    }

    private String generateAnswer(String companyName, String questionContent) {
        if (questionContent.contains("코딩 테스트")) {
            return companyName + " 코딩 테스트는 알고리즘 2문제와 SQL 1문제로 구성되었습니다. 난이도는 프로그래머스 레벨 3 정도였고, 시간은 총 2시간 주어졌습니다. 자료구조와 그래프 탐색 유형이 많이 나왔습니다.";
        }
        if (questionContent.contains("면접 시간")) {
            return "1차 기술면접은 약 1시간, 2차 인성면접은 약 45분 정도 소요되었습니다. 각 면접 사이에 일주일 정도 간격이 있었고, 최종 결과는 2차 면접 후 약 2주 뒤에 통보받았습니다.";
        }
        if (questionContent.contains("라이브 코딩")) {
            return "네, 기술면접 중간에 간단한 라이브 코딩이 있었습니다. 화이트보드에 의사코드를 작성하는 형태였고, 완벽한 코드보다는 문제 해결 접근 방식과 커뮤니케이션을 더 중시하는 느낌이었습니다.";
        }
        if (questionContent.contains("연봉")) {
            return "연봉 협상은 최종 합격 통보 후 진행되었습니다. 희망 연봉을 먼저 물어보셨고, 이전 직장 대비 합리적인 수준으로 제안해주셨습니다. 스톡옵션이나 RSU 관련 사항도 함께 안내받았습니다.";
        }
        if (questionContent.contains("온보딩")) {
            return "온보딩은 약 2주간 진행됩니다. 첫째 주에는 회사 문화와 시스템 교육, 둘째 주부터는 멘토와 함께 실제 업무에 투입됩니다. 3개월간 적응 기간이 있어서 부담 없이 시작할 수 있었습니다.";
        }
        if (questionContent.contains("포트폴리오")) {
            return "개인 프로젝트 2개와 팀 프로젝트 1개를 정리해서 제출했습니다. GitHub 링크와 함께 기술 선택 이유, 트러블슈팅 경험을 문서화한 것이 도움이 되었다고 면접관분이 말씀하셨습니다.";
        }
        if (questionContent.contains("불합격") || questionContent.contains("재지원")) {
            return "불합격 이유는 정확히 알 수 없지만, 기술 면접에서 깊이 있는 답변을 못한 부분이 컸다고 생각합니다. 재지원은 보통 6개월 이후 가능하다고 안내받았습니다. 부족한 부분을 보완해서 다시 도전할 예정입니다.";
        }
        if (questionContent.contains("SQL")) {
            return "SQL 관련 질문은 있었습니다. 윈도우 함수, 서브쿼리 최적화, 인덱스 설계 등을 물어봤고, 실제 쿼리를 작성하는 과제도 있었습니다. SQL 기본기가 탄탄하면 무난하게 통과할 수 있습니다.";
        }
        if (questionContent.contains("과제")) {
            return "별도의 사전 과제는 없었고, 면접 중에 간단한 설계 문제를 풀었습니다. 30분 정도 시간이 주어졌고, 정답보다는 접근 방식과 트레이드오프 분석 능력을 봤습니다.";
        }
        if (questionContent.contains("팀 분위기") || questionContent.contains("문화")) {
            return "팀 분위기는 자율적이고 수평적인 편이었습니다. 면접 과정에서도 '님' 호칭을 사용하셨고, 재택근무와 유연근무제를 적극 활용하고 있다고 하셨습니다.";
        }
        if (questionContent.contains("자격증")) {
            return "자격증이 필수는 아니지만, CKA나 AWS 관련 자격증이 있으면 가산점이 있다고 하셨습니다. 실무 경험을 더 중시하는 편이었고, 자격증보다는 실제 운영 경험을 깊이 물어봤습니다.";
        }
        if (questionContent.contains("온콜") || questionContent.contains("근무")) {
            return "온콜 근무는 팀 내 로테이션으로 운영되고, 보통 2~3주에 한 번 정도 돌아온다고 합니다. 온콜 수당도 별도로 지급되고, 장애 발생 빈도는 높지 않다고 하셨습니다.";
        }
        if (questionContent.contains("준비 팁") || questionContent.contains("어떻게 준비")) {
            return "인프라 기본기와 트러블슈팅 경험을 정리하는 것을 추천합니다. 특히 장애 상황에서의 판단 과정을 구체적으로 설명할 수 있으면 좋고, 최신 기술 트렌드도 파악하고 가시면 좋습니다.";
        }
        if (questionContent.contains("모니터링")) {
            return "Prometheus, Grafana, Datadog 등의 경험을 물어봤습니다. 특히 커스텀 메트릭 설계와 알림 정책 수립 경험을 중시했고, ELK 스택을 활용한 로그 분석 능력도 확인했습니다.";
        }
        if (questionContent.contains("신입")) {
            return "신입 채용도 별도로 진행하고 있습니다. 다만 이 포지션은 경력직 채용이었고, 신입 공채는 보통 상/하반기에 별도로 공고가 올라옵니다. 인턴 전환 채용도 많이 하는 편이라고 들었습니다.";
        }
        if (questionContent.contains("기술 스택") || questionContent.contains("언어")) {
            return "Spring Boot와 Java가 메인이지만, 팀에 따라 Kotlin도 사용한다고 합니다. 코딩 테스트 언어 제한은 없었고 Java, Python, C++ 등 자유롭게 선택할 수 있었습니다.";
        }
        if (questionContent.contains("복장")) {
            return "자유로운 복장으로 참석했습니다. 면접관분들도 캐주얼하게 입고 오셔서 편안한 분위기였습니다. 굳이 정장을 입을 필요는 없지만, 너무 캐주얼하지 않은 스마트 캐주얼 정도면 적당합니다.";
        }
        if (questionContent.contains("팀 배치")) {
            return "입사 전 희망 팀을 3순위까지 제출할 수 있었고, 면접 시 매칭되었던 팀에 우선 배치됩니다. 입사 후에도 내부 이동이 비교적 자유로운 편이라고 합니다.";
        }
        // 기본 답변
        return "좋은 질문이네요. " + companyName + " 면접을 준비할 때 가장 중요한 것은 기본기와 실무 경험의 균형이라고 생각합니다. 공식 기술 블로그와 채용 페이지를 참고하시면 많은 도움이 될 것입니다.";
    }

    // ===== 회사 초기화 =====

    private void initCompanies() {
        log.info("Initializing company seed data...");

        final List<CompanyData> companies = List.of(
                // IT 대기업
                new CompanyData("카카오", "IT", "https://logo.clearbit.com/kakao.com", "https://www.kakaocorp.com"),
                new CompanyData("네이버", "IT", "https://logo.clearbit.com/naver.com", "https://www.navercorp.com"),
                new CompanyData("삼성전자", "전자/반도체", "https://logo.clearbit.com/samsung.com", "https://www.samsung.com"),
                new CompanyData("LG전자", "전자", "https://logo.clearbit.com/lge.com", "https://www.lge.co.kr"),
                new CompanyData("SK텔레콤", "통신", "https://logo.clearbit.com/sktelecom.com", "https://www.sktelecom.com"),
                new CompanyData("KT", "통신", "https://logo.clearbit.com/kt.com", "https://www.kt.com"),
                new CompanyData("LG유플러스", "통신", "https://logo.clearbit.com/lguplus.co.kr", "https://www.lguplus.co.kr"),
                new CompanyData("현대자동차", "자동차", "https://logo.clearbit.com/hyundai.com", "https://www.hyundai.com"),
                new CompanyData("기아자동차", "자동차", "https://logo.clearbit.com/kia.com", "https://www.kia.com"),
                new CompanyData("POSCO", "철강", "https://logo.clearbit.com/posco.co.kr", "https://www.posco.co.kr"),

                // IT 스타트업 & 유니콘
                new CompanyData("토스", "핀테크", "https://logo.clearbit.com/toss.im", "https://toss.im"),
                new CompanyData("배달의민족", "푸드테크", "https://logo.clearbit.com/baemin.com", "https://www.baemin.com"),
                new CompanyData("쿠팡", "이커머스", "https://logo.clearbit.com/coupang.com", "https://www.coupang.com"),
                new CompanyData("당근마켓", "플랫폼", "https://logo.clearbit.com/daangn.com", "https://www.daangn.com"),
                new CompanyData("카카오뱅크", "핀테크", "https://logo.clearbit.com/kakaobank.com", "https://www.kakaobank.com"),
                new CompanyData("카카오페이", "핀테크", "https://logo.clearbit.com/kakaopay.com", "https://www.kakaopay.com"),
                new CompanyData("네이버파이낸셜", "핀테크", null, "https://www.naverfin.com"),
                new CompanyData("라인", "메신저/플랫폼", "https://logo.clearbit.com/line.me", "https://line.me"),
                new CompanyData("크래프톤", "게임", "https://logo.clearbit.com/krafton.com", "https://www.krafton.com"),
                new CompanyData("넷마블", "게임", "https://logo.clearbit.com/netmarble.com", "https://www.netmarble.com"),

                // 글로벌 IT 기업 한국지사
                new CompanyData("구글코리아", "IT", "https://logo.clearbit.com/google.com", "https://www.google.co.kr"),
                new CompanyData("마이크로소프트", "IT", "https://logo.clearbit.com/microsoft.com", "https://www.microsoft.com"),
                new CompanyData("메타코리아", "IT", "https://logo.clearbit.com/meta.com", "https://www.meta.com"),
                new CompanyData("아마존코리아", "이커머스/클라우드", "https://logo.clearbit.com/amazon.com", "https://www.amazon.co.kr"),
                new CompanyData("넷플릭스코리아", "OTT", "https://logo.clearbit.com/netflix.com", "https://www.netflix.com"),

                // 금융
                new CompanyData("신한은행", "금융", "https://logo.clearbit.com/shinhan.com", "https://www.shinhan.com"),
                new CompanyData("국민은행", "금융", "https://logo.clearbit.com/kb.co.kr", "https://www.kbstar.com"),
                new CompanyData("우리은행", "금융", "https://logo.clearbit.com/wooribank.com", "https://www.wooribank.com"),
                new CompanyData("하나은행", "금융", "https://logo.clearbit.com/hanabank.com", "https://www.hanabank.com"),
                new CompanyData("NH농협은행", "금융", "https://logo.clearbit.com/nonghyup.com", "https://banking.nonghyup.com"),

                // 스타트업
                new CompanyData("버킷플레이스", "플랫폼", null, "https://www.bucketplace.co.kr"),
                new CompanyData("야놀자", "여행/숙박", "https://logo.clearbit.com/yanolja.com", "https://www.yanolja.com"),
                new CompanyData("직방", "부동산테크", "https://logo.clearbit.com/zigbang.com", "https://www.zigbang.com"),
                new CompanyData("뱅크샐러드", "핀테크", "https://logo.clearbit.com/banksalad.com", "https://www.banksalad.com"),
                new CompanyData("원티드", "HR테크", "https://logo.clearbit.com/wanted.co.kr", "https://www.wanted.co.kr"),
                new CompanyData("로켓펀치", "HR테크", "https://logo.clearbit.com/rocketpunch.com", "https://www.rocketpunch.com"),
                new CompanyData("프로그래머스", "에듀테크", "https://logo.clearbit.com/programmers.co.kr", "https://programmers.co.kr"),
                new CompanyData("패스트캠퍼스", "에듀테크", "https://logo.clearbit.com/fastcampus.co.kr", "https://www.fastcampus.co.kr"),
                new CompanyData("인프런", "에듀테크", "https://logo.clearbit.com/inflearn.com", "https://www.inflearn.com"),
                new CompanyData("29CM", "이커머스/패션", "https://logo.clearbit.com/29cm.co.kr", "https://www.29cm.co.kr"),
                new CompanyData("무신사", "패션", "https://logo.clearbit.com/musinsa.com", "https://www.musinsa.com"),
                new CompanyData("컬리", "이커머스/식품", "https://logo.clearbit.com/kurly.com", "https://www.kurly.com"),
                new CompanyData("번개장터", "중고거래", "https://logo.clearbit.com/bunjang.co.kr", "https://www.bunjang.co.kr"),
                new CompanyData("중고나라", "중고거래", "https://logo.clearbit.com/joongna.com", "https://www.joongna.com"),

                // IT 서비스
                new CompanyData("NHN", "IT서비스", "https://logo.clearbit.com/nhn.com", "https://www.nhn.com"),
                new CompanyData("카카오엔터프라이즈", "IT서비스", null, "https://www.kakaoenterprise.com"),
                new CompanyData("네이버클라우드플랫폼", "클라우드", null, "https://www.ncloud.com"),
                new CompanyData("라인플러스", "IT서비스", null, "https://linepluscorp.com"),
                new CompanyData("데브시스터즈", "게임", "https://logo.clearbit.com/devsisters.com", "https://www.devsisters.com"),
                new CompanyData("스마일게이트", "게임", "https://logo.clearbit.com/smilegate.com", "https://www.smilegate.com"),
                new CompanyData("엔씨소프트", "게임", "https://logo.clearbit.com/ncsoft.com", "https://www.ncsoft.com"),
                new CompanyData("넥슨", "게임", "https://logo.clearbit.com/nexon.com", "https://www.nexon.com")
        );

        companies.forEach(companyData -> {
            if (!companyRepository.existsByName(companyData.name())) {
                final Company company = Company.from(
                        companyData.name(),
                        companyData.industry(),
                        companyData.logoUrl(),
                        companyData.website()
                );
                companyRepository.save(company);
            }
        });

        log.info("Company seed data initialization completed. Total companies: {}", companies.size());
    }

    // ===== 유틸리티 =====

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Data initialization interrupted");
        }
    }

    // ===== 내부 레코드 =====

    private record CompanyData(String name, String industry, String logoUrl, String website) {}

    private record ReviewData(String companyName, String position, InterviewResult result,
                              int difficulty, int atmosphere, LocalDate interviewDate,
                              List<String> interviewTypes, List<String> questions, String content) {}

    private record QaData(List<String> questions) {}
}