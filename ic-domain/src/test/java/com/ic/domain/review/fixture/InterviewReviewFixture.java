package com.ic.domain.review.fixture;

import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;
import com.github.javafaker.Faker;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * InterviewReview 테스트 픽스처
 */
public class InterviewReviewFixture {

    private static final Faker faker = new Faker(new Locale("ko"));

    public static InterviewReview 기본후기(Member member, Company company) {
        return InterviewReview.create(
                member,
                company,
                LocalDate.now().minusDays(faker.number().numberBetween(1, 30)),
                "백엔드 개발자",
                Arrays.asList("화상면접", "코딩테스트"),
                Arrays.asList("자기소개를 해주세요", "왜 이 회사를 지원했나요?"),
                3,
                4,
                InterviewResult.PASS,
                "전반적으로 좋은 면접 경험이었습니다."
        );
    }

    public static InterviewReview 프론트엔드후기(Member member, Company company) {
        return InterviewReview.create(
                member,
                company,
                LocalDate.now().minusDays(faker.number().numberBetween(1, 30)),
                "프론트엔드 개발자",
                Arrays.asList("대면면접", "포트폴리오 발표"),
                Arrays.asList("리액트 경험은 어느 정도인가요?", "최근 프로젝트에서 어려웠던 점은?"),
                4,
                3,
                InterviewResult.FAIL,
                "기술적인 질문이 많았습니다."
        );
    }

    public static InterviewReview 어려운후기(Member member, Company company) {
        return InterviewReview.create(
                member,
                company,
                LocalDate.now().minusDays(faker.number().numberBetween(1, 30)),
                "시니어 개발자",
                Arrays.asList("대면면접", "화이트보드 코딩"),
                Arrays.asList("시스템 설계를 해보세요", "대용량 트래픽을 어떻게 처리할 건가요?"),
                5,
                2,
                InterviewResult.PASS,
                "매우 도전적인 면접이었습니다."
        );
    }

    public static InterviewReviewBuilder builder() {
        return new InterviewReviewBuilder();
    }

    public static class InterviewReviewBuilder {
        private Member member;
        private Company company;
        private LocalDate interviewDate = LocalDate.now().minusDays(7);
        private String position = "개발자";
        private List<String> interviewTypes = Arrays.asList("면접");
        private List<String> questions = Arrays.asList("질문");
        private int difficulty = 3;
        private int atmosphere = 3;
        private InterviewResult result = InterviewResult.PASS;
        private String content = "후기 내용";

        public InterviewReviewBuilder member(Member member) {
            this.member = member;
            return this;
        }

        public InterviewReviewBuilder company(Company company) {
            this.company = company;
            return this;
        }

        public InterviewReviewBuilder interviewDate(LocalDate interviewDate) {
            this.interviewDate = interviewDate;
            return this;
        }

        public InterviewReviewBuilder position(String position) {
            this.position = position;
            return this;
        }

        public InterviewReviewBuilder interviewTypes(List<String> interviewTypes) {
            this.interviewTypes = interviewTypes;
            return this;
        }

        public InterviewReviewBuilder questions(List<String> questions) {
            this.questions = questions;
            return this;
        }

        public InterviewReviewBuilder difficulty(int difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public InterviewReviewBuilder atmosphere(int atmosphere) {
            this.atmosphere = atmosphere;
            return this;
        }

        public InterviewReviewBuilder result(InterviewResult result) {
            this.result = result;
            return this;
        }

        public InterviewReviewBuilder content(String content) {
            this.content = content;
            return this;
        }

        public InterviewReview build() {
            return InterviewReview.create(
                    member,
                    company,
                    interviewDate,
                    position,
                    interviewTypes,
                    questions,
                    difficulty,
                    atmosphere,
                    result,
                    content
            );
        }
    }
}