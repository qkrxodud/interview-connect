package com.ic.domain.review;

import com.ic.common.entity.BaseTimeEntity;
import com.ic.domain.company.Company;
import com.ic.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "interview_reviews")
public class InterviewReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(nullable = false, length = 50)
    private String position;

    @Convert(converter = StringListConverter.class)
    @Column(name = "interview_types", columnDefinition = "TEXT")
    private List<String> interviewTypes;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> questions;

    @Column(nullable = false)
    private int difficulty;

    @Column(nullable = false)
    private int atmosphere;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewResult result;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count")
    private long viewCount;

    @Builder
    private InterviewReview(Long id, Member member, Company company, LocalDate interviewDate,
                           String position, List<String> interviewTypes, List<String> questions,
                           Integer difficulty, Integer atmosphere, InterviewResult result, String content, long viewCount) {
        this.id = id;
        this.member = member;
        this.company = company;
        this.interviewDate = interviewDate;
        this.position = position;
        this.interviewTypes = interviewTypes;
        this.questions = questions;
        this.difficulty = difficulty != null ? difficulty : 0;
        this.atmosphere = atmosphere != null ? atmosphere : 0;
        this.result = result;
        this.content = content;
        this.viewCount = viewCount;
    }

    public static InterviewReview create(Member member, Company company, LocalDate interviewDate,
                                       String position, List<String> interviewTypes, List<String> questions,
                                       Integer difficulty, Integer atmosphere, InterviewResult result, String content) {
        if (difficulty == null) {
            throw new IllegalArgumentException("난이도는 필수입니다");
        }
        if (atmosphere == null) {
            throw new IllegalArgumentException("분위기는 필수입니다");
        }
        validateDifficulty(difficulty);
        validateAtmosphere(atmosphere);
        validatePosition(position);

        return InterviewReview.builder()
                .member(member)
                .company(company)
                .interviewDate(interviewDate)
                .position(position)
                .interviewTypes(interviewTypes)
                .questions(questions)
                .difficulty(difficulty)
                .atmosphere(atmosphere)
                .result(result)
                .content(content)
                .viewCount(0L)
                .build();
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changePosition(String position) {
        validatePosition(position);
        this.position = position;
    }

    public void changeDifficulty(Integer difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("난이도는 필수입니다");
        }
        validateDifficulty(difficulty);
        this.difficulty = difficulty;
    }

    public void changeAtmosphere(Integer atmosphere) {
        if (atmosphere == null) {
            throw new IllegalArgumentException("분위기는 필수입니다");
        }
        validateAtmosphere(atmosphere);
        this.atmosphere = atmosphere;
    }

    public void changeResult(InterviewResult result) {
        this.result = result;
    }

    public void changeInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public void changeInterviewTypes(List<String> interviewTypes) {
        this.interviewTypes = interviewTypes;
    }

    public void changeQuestions(List<String> questions) {
        this.questions = questions;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void updateViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isWrittenBy(Long memberId) {
        return Objects.equals(this.member.getId(), memberId);
    }

    public boolean hasContent() {
        return Objects.nonNull(content) && !content.trim().isEmpty();
    }

    public boolean hasQuestions() {
        return Objects.nonNull(questions) && !questions.isEmpty();
    }

    private static void validateDifficulty(int difficulty) {
        if (difficulty < 1 || difficulty > 5) {
            throw new IllegalArgumentException("난이도는 1~5 사이의 값이어야 합니다");
        }
    }

    private static void validateAtmosphere(int atmosphere) {
        if (atmosphere < 1 || atmosphere > 5) {
            throw new IllegalArgumentException("분위기는 1~5 사이의 값이어야 합니다");
        }
    }

    private static void validatePosition(String position) {
        if (Objects.isNull(position) || position.trim().isEmpty()) {
            throw new IllegalArgumentException("포지션은 필수입니다");
        }
        if (position.length() > 50) {
            throw new IllegalArgumentException("포지션은 50자를 초과할 수 없습니다");
        }
    }
}