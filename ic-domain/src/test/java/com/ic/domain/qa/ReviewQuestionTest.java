package com.ic.domain.qa;

import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewReview;
import com.ic.domain.review.InterviewResult;
import com.ic.domain.company.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReviewQuestionTest {

    @Test
    @DisplayName("질문 생성 성공")
    void shouldCreateQuestionSuccessfully() {
        // given
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Company company = Company.from("테스트회사", "IT", null, null);

        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        String content = "면접에서 어떤 질문을 받으셨나요?";

        // when
        ReviewQuestion question = ReviewQuestion.create(review, questioner, content);

        // then
        assertThat(question.getInterviewReview()).isEqualTo(review);
        assertThat(question.getQuestioner()).isEqualTo(questioner);
        assertThat(question.getContent()).isEqualTo(content);
        assertThat(question.getCreatedAt()).isNotNull();
        assertThat(question.getAnswers()).isEmpty();
        assertThat(question.hasAnswers()).isFalse();
        assertThat(question.getAnswerCount()).isZero();
    }

    @Test
    @DisplayName("질문 생성 시 면접 후기가 null이면 예외 발생")
    void shouldThrowExceptionWhenReviewIsNull() {
        // given
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");

        // when & then
        assertThatThrownBy(() -> ReviewQuestion.create(null, questioner, "질문 내용"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("면접 후기는 필수입니다");
    }

    @Test
    @DisplayName("질문 생성 시 질문자가 null이면 예외 발생")
    void shouldThrowExceptionWhenQuestionerIsNull() {
        // given
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Company company = Company.from("테스트회사", "IT", null, null);
        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        // when & then
        assertThatThrownBy(() -> ReviewQuestion.create(review, null, "질문 내용"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("질문자는 필수입니다");
    }

    @Test
    @DisplayName("질문 생성 시 내용이 null이면 예외 발생")
    void shouldThrowExceptionWhenContentIsNull() {
        // given
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Company company = Company.from("테스트회사", "IT", null, null);
        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        // when & then
        assertThatThrownBy(() -> ReviewQuestion.create(review, questioner, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("질문 내용은 필수입니다");
    }

    @Test
    @DisplayName("질문 생성 시 내용이 빈 문자열이면 예외 발생")
    void shouldThrowExceptionWhenContentIsEmpty() {
        // given
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Company company = Company.from("테스트회사", "IT", null, null);
        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        // when & then
        assertThatThrownBy(() -> ReviewQuestion.create(review, questioner, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("질문 내용은 필수입니다");
    }

    @Test
    @DisplayName("질문 생성 시 내용이 500자를 초과하면 예외 발생")
    void shouldThrowExceptionWhenContentExceeds500Characters() {
        // given
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Company company = Company.from("테스트회사", "IT", null, null);
        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        String longContent = "a".repeat(501);

        // when & then
        assertThatThrownBy(() -> ReviewQuestion.create(review, questioner, longContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("질문 내용은 500자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("답변 추가 성공")
    void shouldAddAnswerSuccessfully() {
        // given
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");
        Member answerer = Member.createGeneral("answerer@test.com", "password123", "답변자");
        Company company = Company.from("테스트회사", "IT", null, null);

        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        ReviewQuestion question = ReviewQuestion.create(review, questioner, "질문입니다");
        ReviewAnswer answer = ReviewAnswer.create(question, answerer, "답변입니다");

        // when
        question.addAnswer(answer);

        // then
        assertThat(question.hasAnswers()).isTrue();
        assertThat(question.getAnswerCount()).isEqualTo(1);
        assertThat(question.getAnswers()).contains(answer);
    }
}