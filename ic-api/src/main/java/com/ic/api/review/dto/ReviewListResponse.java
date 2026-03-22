package com.ic.api.review.dto;

import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 면접 후기 목록 응답 DTO (간소화된 정보)
 */
public record ReviewListResponse(
        Long id,
        Long companyId,
        String companyName,
        String memberNickname,
        LocalDate interviewDate,
        String position,
        int difficulty,
        int atmosphere,
        InterviewResult result,
        long viewCount,
        LocalDateTime createdAt
) {

    public static ReviewListResponse from(InterviewReview review) {
        return new ReviewListResponse(
                review.getId(),
                review.getCompany().getId(),
                review.getCompany().getName(),
                review.getMember().getNickname(),
                review.getInterviewDate(),
                review.getPosition(),
                review.getDifficulty(),
                review.getAtmosphere(),
                review.getResult(),
                review.getViewCount(),
                review.getCreatedAt()
        );
    }
}