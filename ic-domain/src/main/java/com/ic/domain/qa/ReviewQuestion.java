package com.ic.domain.qa;

import com.ic.domain.member.Member;
import com.ic.domain.review.InterviewReview;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "review_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_review_id", nullable = false)
    private InterviewReview interviewReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questioner_id", nullable = false)
    private Member questioner;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reviewQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ReviewAnswer> answers = new ArrayList<>();

    @Builder
    private ReviewQuestion(InterviewReview interviewReview, Member questioner, String content) {
        this.interviewReview = Objects.requireNonNull(interviewReview, "면접 후기는 필수입니다");
        this.questioner = Objects.requireNonNull(questioner, "질문자는 필수입니다");
        this.content = Objects.requireNonNull(content, "질문 내용은 필수입니다");
        this.createdAt = LocalDateTime.now();

        validateContent(content);
    }

    public static ReviewQuestion create(InterviewReview interviewReview, Member questioner, String content) {
        return ReviewQuestion.builder()
                .interviewReview(interviewReview)
                .questioner(questioner)
                .content(content)
                .build();
    }

    public void addAnswer(ReviewAnswer answer) {
        if (Objects.nonNull(answer)) {
            this.answers.add(answer);
        }
    }

    public boolean hasAnswers() {
        return !answers.isEmpty();
    }

    public int getAnswerCount() {
        return answers.size();
    }

    private void validateContent(final String content) {
        if (Objects.isNull(content) || content.trim().isEmpty()) {
            throw new IllegalArgumentException("질문 내용은 필수입니다");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("질문 내용은 500자를 초과할 수 없습니다");
        }
    }
}