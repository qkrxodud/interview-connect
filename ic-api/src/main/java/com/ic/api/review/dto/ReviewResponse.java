package com.ic.api.review.dto;

import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 면접 후기 응답 DTO
 */
public record ReviewResponse(
        Long id,
        Long companyId,
        String companyName,
        Long memberId,
        String memberNickname,
        LocalDate interviewDate,
        String position,
        List<String> interviewTypes,
        List<String> questions,
        int difficulty,
        int atmosphere,
        InterviewResult result,
        String content,
        long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ReviewResponse from(InterviewReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getCompany().getId(),
                review.getCompany().getName(),
                review.getMember().getId(),
                review.getMember().getNickname(),
                review.getInterviewDate(),
                review.getPosition(),
                review.getInterviewTypes(),
                review.getQuestions(),
                review.getDifficulty(),
                review.getAtmosphere(),
                review.getResult(),
                review.getContent(),
                review.getViewCount(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}