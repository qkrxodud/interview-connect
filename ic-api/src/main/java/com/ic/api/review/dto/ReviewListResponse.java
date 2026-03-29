package com.ic.api.review.dto;

import com.ic.domain.review.InterviewResult;
import com.ic.domain.review.InterviewReview;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        List<String> interviewTypes,
        int difficulty,
        int atmosphere,
        InterviewResult result,
        String content,
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
                review.getInterviewTypes(),
                review.getDifficulty(),
                review.getAtmosphere(),
                review.getResult(),
                review.getContent(),
                review.getViewCount(),
                review.getCreatedAt()
        );
    }
}