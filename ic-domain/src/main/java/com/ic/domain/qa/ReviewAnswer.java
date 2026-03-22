package com.ic.domain.qa;

import com.ic.common.entity.BaseTimeEntity;
import com.ic.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "review_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_question_id", nullable = false)
    private ReviewQuestion reviewQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerer_id", nullable = false)
    private Member answerer;

    @Column(nullable = false, length = 2000)
    private String content;

    @Builder
    private ReviewAnswer(ReviewQuestion reviewQuestion, Member answerer, String content) {
        this.reviewQuestion = Objects.requireNonNull(reviewQuestion, "질문은 필수입니다");
        this.answerer = Objects.requireNonNull(answerer, "답변자는 필수입니다");
        this.content = Objects.requireNonNull(content, "답변 내용은 필수입니다");

        validateContent(content);
        validateAnswerer();
    }

    public static ReviewAnswer create(ReviewQuestion reviewQuestion, Member answerer, String content) {
        return ReviewAnswer.builder()
                .reviewQuestion(reviewQuestion)
                .answerer(answerer)
                .content(content)
                .build();
    }

    public void updateContent(final String newContent) {
        validateContent(newContent);
        this.content = newContent;
    }

    public boolean isAnsweredBy(final Long memberId) {
        return Objects.nonNull(memberId) && answerer.getId().equals(memberId);
    }

    public boolean isReviewAuthor() {
        final boolean sameId = Objects.nonNull(reviewQuestion.getInterviewReview().getMember().getId()) &&
                              Objects.nonNull(answerer.getId()) &&
                              Objects.equals(reviewQuestion.getInterviewReview().getMember().getId(), answerer.getId());

        final boolean sameEmail = reviewQuestion.getInterviewReview().getMember().getEmail().equals(answerer.getEmail());

        return sameId || sameEmail;
    }

    private void validateContent(final String content) {
        if (Objects.isNull(content) || content.trim().isEmpty()) {
            throw new IllegalArgumentException("답변 내용은 필수입니다");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("답변 내용은 2000자를 초과할 수 없습니다");
        }
    }

    private void validateAnswerer() {
        final boolean sameId = Objects.nonNull(reviewQuestion.getQuestioner().getId()) &&
                              Objects.nonNull(answerer.getId()) &&
                              Objects.equals(reviewQuestion.getQuestioner().getId(), answerer.getId());

        final boolean sameEmail = reviewQuestion.getQuestioner().getEmail().equals(answerer.getEmail());

        if (sameId || sameEmail) {
            throw new IllegalArgumentException("자신이 작성한 질문에는 답변할 수 없습니다");
        }
    }
}