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

class ReviewAnswerTest {

    @Test
    @DisplayName("답변 생성 성공")
    void shouldCreateAnswerSuccessfully() {
        // given
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");
        Member answerer = Member.createGeneral("answerer@test.com", "password123", "답변자");
        Company company = Company.from("테스트회사", "IT", null, null);

        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        ReviewQuestion question = ReviewQuestion.create(review, questioner, "질문입니다");
        String content = "답변 내용입니다";

        // when
        ReviewAnswer answer = ReviewAnswer.create(question, answerer, content);

        // then
        assertThat(answer.getReviewQuestion()).isEqualTo(question);
        assertThat(answer.getAnswerer()).isEqualTo(answerer);
        assertThat(answer.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("질문자가 자신의 질문에 답변하려 하면 예외 발생")
    void shouldThrowExceptionWhenQuestionerTriesToAnswerOwnQuestion() {
        // given
        Member questioner = Member.createGeneral("questioner@test.com", "password123", "질문자");
        Member author = Member.createGeneral("author@test.com", "password123", "작성자");
        Company company = Company.from("테스트회사", "IT", null, null);

        InterviewReview review = InterviewReview.create(author, company, LocalDate.now(),
                "백엔드 개발자", List.of("기술면접"), List.of("알고리즘 문제"),
                4, 5, InterviewResult.PASS, "좋은 면접이었습니다");

        ReviewQuestion question = ReviewQuestion.create(review, questioner, "질문입니다");

        // when & then
        assertThatThrownBy(() -> ReviewAnswer.create(question, questioner, "답변 내용"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자신이 작성한 질문에는 답변할 수 없습니다");
    }
}